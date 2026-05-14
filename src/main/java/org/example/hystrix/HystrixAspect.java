package org.example.hystrix;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.util.Set;
import java.util.concurrent.TimeoutException;

@Component
@Aspect
@ConditionalOnProperty(name="hystrix.enable",havingValue = "true")
public class HystrixAspect {

    private static final Logger logger = LoggerFactory.getLogger(HystrixAspect.class);

    @Autowired
    private HystrixStorage hystrixStorage;

    @Pointcut("@annotation(org.example.hystrix.HystrixTest)")
    public void getHystrix(){}

    /*
    1. 运行前判断是否熔断
    2. 运行失败: 增加失败数，判断是否需要告警，是否需要设置熔断状态
       运行成功: 判断是否超时，增加失败数，判断是否需要告警，是否需要设置熔断状态
                不超时的情况下，增加成功数，判断是否需要告警，是否需要设置熔断状态
     */
    @Around("getHystrix()")
    public Object process(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        HystrixTest hystrix = method.getAnnotation(HystrixTest.class);
        String serviceName = hystrix.name();
        Object result = null;
        if(hystrixStorage.isHystrix(serviceName, hystrix.windowTime())){
            throw new Exception("接口处于熔断状态");
        }
        Long startTime = System.currentTimeMillis();
        try{
            result = joinPoint.proceed();
        }catch (Throwable e){
            try{
                if(isErrorException(e)){
                    hystrixStorage.markFailure(serviceName);
                    evaluateCircuitBreaker(serviceName, hystrix);
                }
            }catch (Throwable throwable){
                logger.info("熔断窗口出现错误");

            }
            throw e;
        }
        Long nowTime = System.currentTimeMillis();
        try {
            if (hystrix.TimeOut() == 1 && nowTime - startTime >= hystrix.Time()) {
                hystrixStorage.markFailure(serviceName);
            } else {
                hystrixStorage.markTotal(serviceName);
            }
            evaluateCircuitBreaker(serviceName, hystrix);
        }catch (Exception e){
            logger.info("熔断器问题");
        }
        return result;
    }

    /* 判断是否告警以及熔断 */
    public void evaluateCircuitBreaker(String serviceName, HystrixTest hystrix){
        HystrixDataSnap dataSnap = hystrixStorage.getHystrixDataSnap(serviceName, hystrix.windowTime());
        int windowSize = hystrix.windowTime();
        if(hystrix.alertRequestCount() <= dataSnap.getTotalCount() && hystrix.alertErrorRate() <= dataSnap.getErrorRate()){
            boolean isNeedAlert = hystrixStorage.setAlert(serviceName, windowSize);
            if(isNeedAlert){
                // TODO: 记录告警日志
                logger.info("isAlert");
            }
        }
        if(hystrix.circuitRequestCount() <= dataSnap.getTotalCount() && hystrix.circuitErrorRate() <= dataSnap.getErrorRate()){
            boolean isNeedHystrix = hystrixStorage.setHystrix(serviceName);
            if(isNeedHystrix){
                // TODO: 记录熔断日志
                logger.info(("isHystrix"));
            }
        }
    }

    public boolean isErrorException(Throwable e){
        Set<Class<?>> IGNORED_EXCEPTIONS = Set.of(TimeoutException.class, IOException.class, ConnectException.class,
                HttpServerErrorException.class);
        for(Class<?> clz: IGNORED_EXCEPTIONS){
            if(clz.isInstance(e)){
                return true;
            }
        }
        return false;
    }
}
