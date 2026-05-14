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
import java.lang.reflect.Method;

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

        }
        Long startTime = System.currentTimeMillis();
        try{
            result = joinPoint.proceed();
        }catch (Throwable e){
            boolean status = hystrixStorage.markFailure(serviceName);
            if(!status){
                // TODO: 记录熔断容器出错日志
            }
            evaluateCircuitBreaker(serviceName, hystrix);
            throw e;
        }
        if(hystrix.TimeOut() == 1){
            Long nowTime = System.currentTimeMillis();
            if(nowTime - startTime >= hystrix.Time()){
                boolean status = hystrixStorage.markFailure(serviceName);
                if(!status){
                    //TODO:
                }
                evaluateCircuitBreaker(serviceName, hystrix);
            }
            hystrixStorage.markSuccess(serviceName);
            evaluateCircuitBreaker(serviceName, hystrix);
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
}
