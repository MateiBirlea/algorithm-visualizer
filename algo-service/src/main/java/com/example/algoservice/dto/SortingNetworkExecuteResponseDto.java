package com.example.algoservice.dto;

import com.example.algoservice.model.SortingNetworkAlgorithm;

import java.util.List;

public class SortingNetworkExecuteResponseDto {
    private List<Integer> initialValues;
    private List<SortingStepDto> steps;
    private SortingMetricsDto metrics;
    private SortingNetworkAlgorithm requestedAlgorithm;
    private SortingNetworkAlgorithm effectiveAlgorithm;
    private SortingNetworkAlgorithm algorithmName;
    private SortingNetworkAlgorithm executedAs;
    private List<Integer> finalArray;
    private Integer totalSteps;
    private Integer totalComparisons;
    private Integer totalSwaps;
    private Long executionTimeMs;
    private Boolean correctlySorted;

    public List<Integer> getInitialValues() {
        return initialValues;
    }

    public void setInitialValues(List<Integer> initialValues) {
        this.initialValues = initialValues;
    }

    public List<SortingStepDto> getSteps() {
        return steps;
    }

    public void setSteps(List<SortingStepDto> steps) {
        this.steps = steps;
    }

    public SortingMetricsDto getMetrics() {
        return metrics;
    }

    public void setMetrics(SortingMetricsDto metrics) {
        this.metrics = metrics;
    }

    public SortingNetworkAlgorithm getRequestedAlgorithm() {
        return requestedAlgorithm;
    }

    public void setRequestedAlgorithm(SortingNetworkAlgorithm requestedAlgorithm) {
        this.requestedAlgorithm = requestedAlgorithm;
    }

    public SortingNetworkAlgorithm getEffectiveAlgorithm() {
        return effectiveAlgorithm;
    }

    public void setEffectiveAlgorithm(SortingNetworkAlgorithm effectiveAlgorithm) {
        this.effectiveAlgorithm = effectiveAlgorithm;
    }

    public SortingNetworkAlgorithm getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(SortingNetworkAlgorithm algorithmName) {
        this.algorithmName = algorithmName;
    }

    public SortingNetworkAlgorithm getExecutedAs() {
        return executedAs;
    }

    public void setExecutedAs(SortingNetworkAlgorithm executedAs) {
        this.executedAs = executedAs;
    }

    public List<Integer> getFinalArray() {
        return finalArray;
    }

    public void setFinalArray(List<Integer> finalArray) {
        this.finalArray = finalArray;
    }

    public Integer getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(Integer totalSteps) {
        this.totalSteps = totalSteps;
    }

    public Integer getTotalComparisons() {
        return totalComparisons;
    }

    public void setTotalComparisons(Integer totalComparisons) {
        this.totalComparisons = totalComparisons;
    }

    public Integer getTotalSwaps() {
        return totalSwaps;
    }

    public void setTotalSwaps(Integer totalSwaps) {
        this.totalSwaps = totalSwaps;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public Boolean getCorrectlySorted() {
        return correctlySorted;
    }

    public void setCorrectlySorted(Boolean correctlySorted) {
        this.correctlySorted = correctlySorted;
    }
}
