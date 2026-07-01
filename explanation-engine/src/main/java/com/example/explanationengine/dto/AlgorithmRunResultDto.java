package com.example.explanationengine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class AlgorithmRunResultDto {

    @NotBlank
    private String algorithmName;

    @NotBlank
    private String executedAs;

    @NotNull
    private Integer totalSteps;

    @NotNull
    private Integer totalComparisons;

    @NotNull
    private Integer totalSwaps;

    @NotNull
    private Long executionTimeMs;

    @NotNull
    private Boolean correctlySorted;

    @NotNull
    private List<Integer> finalArray;

    public String getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public String getExecutedAs() {
        return executedAs;
    }

    public void setExecutedAs(String executedAs) {
        this.executedAs = executedAs;
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

    public List<Integer> getFinalArray() {
        return finalArray;
    }

    public void setFinalArray(List<Integer> finalArray) {
        this.finalArray = finalArray;
    }
}
