package org.example.hystrix;

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
    /* 最小统计窗口大小*/
    private static final int timeWindowsLength = 2;
    /* 配置时间计算窗口起点 */
    private volatile long firstTimeMs;
    /* 默认接口熔断时间 */
    private final long windowDurationMs = 1000L * 60;

    /* 熔断窗口数组初始化 */
    public HystrixStorage(int windowSize) {
        this.timeWindows = new TimeWindow[windowSize];
        for(int i = 0; i < windowSize; i++){
            timeWindows[i] = new TimeWindow();
        }
        this.firstTimeMs = System.currentTimeMillis();
    }

    /* 获取当前时间窗口 */
    public int getWindowPoint(){
        long nowTime = System.currentTimeMillis();
        if(nowTime - firstTimeMs < 0){
            resetStartTime();
            nowTime = System.currentTimeMillis();
        }
        long minutes = TimeUnit.MILLISECONDS.toMinutes(nowTime - firstTimeMs);
        return (int) minutes % timeWindows.length;
    }

    /* 重新设置开始时间 */
    public synchronized void resetStartTime(){
        if(System.currentTimeMillis() < firstTimeMs){
            firstTimeMs = System.currentTimeMillis();
            int curSizeLength = timeWindows.length - 1;
            while(curSizeLength >= 0){
                timeWindows[curSizeLength] = new TimeWindow();
                curSizeLength--;
            }
        }
    }

    /* 新增成功请求 */
    public boolean markTotal(String key){
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

    /* 获取窗口内接口数值 */
    public HystrixDataSnap getHystrixDataSnap(String key, int windowSize){
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
            }
            point = (point + timeWindows.length - 1) % timeWindows.length;
            windowSize--;
        }
        return new HystrixDataSnap(total, error);
    }

    /* 获取接口熔断状态
    * 1.若 map.get(key) 为null: 返回 false
    * 2.若 map.get(key) 非空: 1. 当前时间 - 熔断开始时间 < 熔断时间: 返回 true
    *                        2. 当前时间 - 熔断开始时间 > 熔断时间: 熔断已过，重置时间窗口内所有档期啊接口的熔断数值。
    *                           删除 key, 返回 false
    *  */
    public boolean isHystrix(String key, int windowSize){
        Long hystrixTime = isHystrixMap.get(key);
        long nowTime = System.currentTimeMillis();
        if(hystrixTime == null){
            return false;
        }else if(nowTime - hystrixTime < windowDurationMs * windowSize){
            return true;
        }else{
            isHystrixMap.remove(key);
            resetTimeWindow(key, windowSize);
            return false;
        }
    }

    /* 设置接口状态为熔断
    *  当第一次设置熔断时，同步进行日志打印
    */
    public boolean setHystrix(String key){
        long startTime = System.currentTimeMillis();
        if (isHystrixMap.containsKey(key)){
            return false;
        }
        Long result = isHystrixMap.putIfAbsent(key, startTime);
        return result == null;
    }

    /*
     * 当接口脱离熔断窗口时，将原本的历史数据进行 reset
     */
    public void resetTimeWindow(String key, int windowSize){
        int point = getWindowPoint();
        while(windowSize > 0){
            TimeWindow timeWindow = timeWindows[point];
            if(timeWindow != null){
                timeWindow.getMap().put(key, new HystrixData());
            }
            point = (point + timeWindows.length - 1) % timeWindows.length;
            windowSize--;
        }
    }

    /* 判断是否需要告警, 如果首次设置告警，需要打印日志 */
    public boolean setAlert(String key, int windowSize){
        Long value = isAlertMap.get(key);
        long nowTime = System.currentTimeMillis();
        if(value == null){
            isAlertMap.putIfAbsent(key, nowTime);
            return true;
        }else if (nowTime - value < windowSize * windowDurationMs){
            return false;
        }else{
            isAlertMap.remove(key);
            return true;
        }
    }
}

