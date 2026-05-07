package org.example.hystrix;


import java.util.concurrent.ConcurrentHashMap;

/**
 * 使用 HashMap 记录接口以及对应 接口熔断数值
 * 接口名，接口熔断数值对象
 */
public class TimeWindow {
    private final ConcurrentHashMap<String, HystrixData> hystrixDataMap = new ConcurrentHashMap<>();
}
