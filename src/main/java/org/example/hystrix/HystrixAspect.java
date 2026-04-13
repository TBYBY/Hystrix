package org.example.hystrix;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Aspect
@EnableConfigurationProperties()
public class HystrixAspect {

    @Pointcut("@annotation(org.example.hystrix.HystrixTest)")
    public void getHystrix(){}

    /**
     * 定义缓存，将 hystrix 的值按照 <Method, 存入缓存
     */
    private static final ConcurrentHashMap<String, HystrixTest> hystrixValueMap = new ConcurrentHashMap<>();

    @Around("getHystrix()")
    public Object process(ProceedingJoinPoint joinPoint) throws Throwable{
        Object result = null;
        String key = null;
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        HystrixTest hystrix = method.getAnnotation(HystrixTest.class);
        key = hystrix.name();
        // 判断当前方法是否将 Hystrix 参数存入,使用原子更新,以免多线程并发冲突
        hystrixValueMap.putIfAbsent(key, hystrix);

        return result;
    }
}
