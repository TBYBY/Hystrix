package org.example.hystrix;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 负责存储接口的熔断状态, 接口的熔断数值
 */


public class HystrixStorage {
    /* 记录当前接口熔断状态 */
    private final ConcurrentHashMap<String, Boolean> isHystrixMap = new ConcurrentHashMap<>();
    /* 记录当前接口熔断数值 */
    private final ConcurrentHashMap<String, HystrixData> hystrixDataMap = new ConcurrentHashMap<>();

    /* 获取当前接口熔断状态*/
    public boolean isHystrix(String key){
        // TODO
        return false;
    }

    /* 获取当前接口熔断数值*/
    public HystrixData getHystrix(String key){
        return hystrixDataMap.computeIfAbsent(key, k -> new HystrixData());
    }

}
