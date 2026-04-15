package org.example.hystrix;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Aspect
@ConditionalOnProperty(name="hystrix.enable",havingValue = "true")
public class HystrixAspect {

    @Pointcut("@annotation(org.example.hystrix.HystrixTest)")
    public void getHystrix(){}

    @Around("getHystrix()")
    public Object process(ProceedingJoinPoint joinPoint) throws Throwable{
        Object result = null;
        String key = null;
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        HystrixTest hystrix = method.getAnnotation(HystrixTest.class);
        key = hystrix.name();

        return result;
    }
}
