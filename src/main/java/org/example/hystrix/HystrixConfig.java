package org.example.hystrix;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HystrixConfig {

    @Bean
    public HystrixStorage hystrixStorage(){
        return new HystrixStorage();
    }
}
