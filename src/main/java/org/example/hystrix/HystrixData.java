package org.example.hystrix;


import java.util.concurrent.atomic.LongAdder;

public class HystrixData {
    /*记录总数*/
    private LongAdder totalCount = new LongAdder();
    /*记录错误请求数*/
    private LongAdder errorCount = new LongAdder();

    public Double getErrorRate(){
        long total_count = totalCount.sum();
        long bad_count = errorCount.sum();
        return (double) bad_count / total_count;
    }

    public void addTotalCount() {
        totalCount.increment();
    }

    public void addErrorCount(){
        errorCount.increment();
    }

    public long getTotalCount(){
        return totalCount.sum();
    }

    public long getErrorCount(){
        return errorCount.sum();
    }

    public void setTotalCount(LongAdder count){
        totalCount = count;
    }


    public void setErrorCount(LongAdder count){
        errorCount = count;
    }
}
