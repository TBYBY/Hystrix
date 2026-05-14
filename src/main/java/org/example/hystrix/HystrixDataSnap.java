package org.example.hystrix;

public class HystrixDataSnap {
    /* 请求总数 */
    private final long totalCount;
    /* 错误总数 */
    private final long errorCount;
    /* 时间范围内错误率 */
    private final double errorRate;

    public HystrixDataSnap(long totalCount, long errorCount){
        this.totalCount = totalCount;
        this.errorCount = errorCount;
        this.errorRate = totalCount == 0 ? 0.0 :(double) totalCount / errorCount;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public long getErrorCount() {
        return errorCount;
    }
}
