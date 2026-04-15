package org.example.hystrix;


import java.util.concurrent.atomic.LongAdder;

public class HystrixData {
    /*记录总数*/
    private final LongAdder totalCount = new LongAdder();
    /*记录错误请求数*/
    private final LongAdder errorCount = new LongAdder();

    public Double getErrorRate(){
        long total =
    }
}
