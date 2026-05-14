package org.example.hystrix;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HystrixTest {
    /**
     * 定义方法级别熔断注解,可根据方法自动配置
     */
    String name() default ""; //记录方法名称
    int alertRequestCount() default 100 ; //告警最小请求数
    double alertErrorRate() default 0.3; //告警失败率
    double circuitErrorRate() default 0.8; //熔断失败率
    int circuitRequestCount() default 200; //熔断最小数
    int windowTime() default 1; //时间窗口
    TimeUnit UNIT() default TimeUnit.MINUTES; // 定义时间单位为分钟
    int TimeOut() default 0; //是否判断接口超时
    int Time() default 0; //接口超时时间阈值
}
