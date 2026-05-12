package org.example.hystrix;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 使用 HashMap 记录接口以及对应 接口熔断数值
 * 接口名，接口熔断数值对象
 */
public class TimeWindow {
    /* 记录当前所有接口的数据 */
    private ConcurrentHashMap<String, HystrixData> hystrixDataMap;

    public ConcurrentHashMap<String, HystrixData> getMap(){
        return hystrixDataMap;
    }

    public void setMap(ConcurrentHashMap<String, HystrixData> map){
        this.hystrixDataMap = map;
    }

    public TimeWindow(){
        this.hystrixDataMap = new ConcurrentHashMap<>();
    }

}
