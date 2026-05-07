package org.example.hystrix;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 负责存储接口的熔断状态, 接口的熔断数值
 * 使用两个 HashMap 分别记录当前 接口熔断状态，接口告警状状态
 * 熔断状态记录： 接口名， 开始熔断时间
 * 接口告警状态记录：接口名，告警开始时间
 * 使用 数组记录 当前时间下 所有接口的熔断数值
 */


public class HystrixStorage {
    /* 记录当前接口熔断状态 */
    private final ConcurrentHashMap<String, Long> isHystrixMap = new ConcurrentHashMap<>();
    /* 记录当前接口告警状态 */
    private final ConcurrentHashMap<String, Long> isAlertMap = new ConcurrentHashMap<>();
    /* 记录当前时间接口熔断数值 */
    /* 获取当前接口熔断状态*/
    public boolean isHystrix(String key, long timeValue){
        long systemTime = System.currentTimeMillis();
        boolean result = false;
        /* 若已过熔断时间，开放窗口*/
        if(isHystrixMap.containsKey(key)){
            result = isHystrixMap.get(key) + timeValue <= systemTime;
        }else{
            isHystrixMap.putIfAbsent(key, systemTime);
        }
        return result;
    }

    /* 获取当前接口熔断状态*/
    public boolean isAlert(String key, long timeValue){
        long systemTime = System.currentTimeMillis();
        boolean result = false;
        /* 若已过熔断时间，开放窗口*/
        if(isAlertMap.containsKey(key)){
            result = isAlertMap.get(key) + timeValue <= systemTime;
        }else{
            isAlertMap.putIfAbsent(key, systemTime);
        }
        return result;
    }

    /* 获取当前接口熔断数值*/
    public HystrixData getHystrix(String key){


        return null;
    }

}
