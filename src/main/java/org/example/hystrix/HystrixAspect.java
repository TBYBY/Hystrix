package org.example.hystrix;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Aspect
public class HystrixAspect {

    @Pointcut("@annotation(org.example.hystrix.HystrixTest)")
    public void getHystrix(){}

    @Autowired
    HystrixStorage hystrixStorage;



    @Around("getHystrix()")
    public Object process(ProceedingJoinPoint joinPoint) throws Throwable{
        Object result = null;
        String key = null;
        /* 获取方法及对应熔断参数 **/
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        HystrixTest hystrix = method.getAnnotation(HystrixTest.class);
        key = hystrix.name();
        try{
            result = joinPoint.proceed();
        } catch (Throwable e) {
            //计算失败请求数，失败率

            throw new RuntimeException(e);
        }

        return result;
    }
}
