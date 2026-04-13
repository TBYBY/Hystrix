package org.example.service;

import org.example.hystrix.HystrixTest;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TestService {

    @HystrixTest(name = "Test1", alertRequestCount = 100, alertErrorRate = 0.3, circuitRequestCount = 250, circuitErrorRate = 0.7, windowTime = 1, UNIT = TimeUnit.MINUTES, TimeOut = 1)
    public int process(){
        return 1;
    }
}
