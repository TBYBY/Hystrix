package org.example.hystrix;

import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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
    /* 记录当前接口的熔断数据*/
    private final TimeWindow[] timeWindows;
    /* 设置窗口默认大小*/
    private static final int windowSize = 10;
    /* 配置时间计算窗口起点 */
    private volatile long firstTime;

    /* 熔断窗口数组初始化 */
    public HystrixStorage(int windowSize) {
        this.timeWindows = new TimeWindow[windowSize];
        for(int i = 0; i < windowSize; i++){
            timeWindows[i] = new TimeWindow();
        }
        this.firstTime = System.currentTimeMillis();
    }

    /* 获取当前时间窗口 */
    public int getWindowPoint(){
        long nowTime = System.currentTimeMillis();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(nowTime - firstTime);
        return (int) minutes % windowSize;
    }

    /* 新增成功请求 */
    public boolean markSuccess(String key){
        int point = getWindowPoint();
        TimeWindow timeWindow = timeWindows[point];
        if(timeWindow == null) {
            return false;
        }
        HystrixData result = timeWindow.getMap().computeIfAbsent(key, k -> new HystrixData());
        result.addTotalCount();
        return true;
    }

    /* 新增失败请求 */
    public boolean markFailure(String key){
        int point = getWindowPoint();
        TimeWindow timeWindow = timeWindows[point];
        if(timeWindow == null) {
            return false;
        }
        HystrixData result = timeWindow.getMap().computeIfAbsent(key, k -> new HystrixData());
        result.addTotalCount();
        result.addErrorCount();
        return true;
    }

    /* 获取接口失败率 */
    public double getFailureRate(String key, int windowSize){
        int point = getWindowPoint();
        long total = 0;
        long error = 0;
        while(windowSize > 0){
            TimeWindow window = timeWindows[point];
            if(window != null){
                HystrixData data = window.getMap().get(key);
                if(data != null){
                    total += data.getTotalCount();
                    error += data.getErrorCount();
                }
                point--;
                if(point < 0){
                    point = timeWindows.length - 1;
                }
                windowSize--;
            }
        }
        return (double) error / total;
    }





}
