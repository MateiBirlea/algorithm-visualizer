package com.example.algoservice.dto;

public class SortingMetricsDto {
    private int totalSteps;
    private int totalComparisons;
    private int totalSwaps;
    private long executionTimeMs;

    public int getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
    }

    public int getTotalComparisons() {
        return totalComparisons;
    }

    public void setTotalComparisons(int totalComparisons) {
        this.totalComparisons = totalComparisons;
    }

    public int getTotalSwaps() {
        return totalSwaps;
    }

    public void setTotalSwaps(int totalSwaps) {
        this.totalSwaps = totalSwaps;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
}
