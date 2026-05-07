package org.example.hystrix;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 熔断容器的初始化受 application 中的配置进行
 * 使用 @Configuration 以及 @ConditionalOnProperty 进行条件初始化，在不开启熔断的情况下不进行开启
 */

@ConditionalOnProperty(value = "hystrix.enable", havingValue = "true")
@Configuration
public class HystrixConfig {

    @Bean
    public HystrixStorage hystrixStorage(){
        return new HystrixStorage();
    }
}
