package com.example.explanationengine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public class RunAnalysisResultDto {

    @NotBlank
    private String algorithm;

    @NotBlank
    private String effectiveAlgorithm;

    @NotNull
    private Integer totalSteps;

    @NotNull
    private Integer totalComparisons;

    @NotNull
    private Integer totalSwaps;

    @NotNull
    private Long executionTimeMs;

    private String algorithmName;
    private String sortDirection;
    private List<Integer> initialArray;
    private List<Integer> finalArray;
    private Integer elementCount;
    private Boolean correctlySorted;
    private Boolean isInitialArraySortedAsc;
    private Boolean isInitialArraySortedDesc;
    private Boolean isFinalArraySortedCorrectly;
    private Integer theoreticalComparisons;
    private Integer actualComparisons;
    private Boolean comparisonMatch;
    private Boolean hasFixedComparisonCount;
    private Boolean earlyStoppingUsed;
    private Boolean comparedWithOtherAlgorithm;
    private List<Map<String, Object>> otherAlgorithmStats;

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getEffectiveAlgorithm() {
        return effectiveAlgorithm;
    }

    public void setEffectiveAlgorithm(String effectiveAlgorithm) {
        this.effectiveAlgorithm = effectiveAlgorithm;
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

    public String getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public List<Integer> getInitialArray() {
        return initialArray;
    }

    public void setInitialArray(List<Integer> initialArray) {
        this.initialArray = initialArray;
    }

    public List<Integer> getFinalArray() {
        return finalArray;
    }

    public void setFinalArray(List<Integer> finalArray) {
        this.finalArray = finalArray;
    }

    public Integer getElementCount() {
        return elementCount;
    }

    public void setElementCount(Integer elementCount) {
        this.elementCount = elementCount;
    }

    public Boolean getCorrectlySorted() {
        return correctlySorted;
    }

    public void setCorrectlySorted(Boolean correctlySorted) {
        this.correctlySorted = correctlySorted;
    }

    public Boolean getIsInitialArraySortedAsc() {
        return isInitialArraySortedAsc;
    }

    public void setIsInitialArraySortedAsc(Boolean isInitialArraySortedAsc) {
        this.isInitialArraySortedAsc = isInitialArraySortedAsc;
    }

    public Boolean getIsInitialArraySortedDesc() {
        return isInitialArraySortedDesc;
    }

    public void setIsInitialArraySortedDesc(Boolean isInitialArraySortedDesc) {
        this.isInitialArraySortedDesc = isInitialArraySortedDesc;
    }

    public Boolean getIsFinalArraySortedCorrectly() {
        return isFinalArraySortedCorrectly;
    }

    public void setIsFinalArraySortedCorrectly(Boolean isFinalArraySortedCorrectly) {
        this.isFinalArraySortedCorrectly = isFinalArraySortedCorrectly;
    }

    public Integer getTheoreticalComparisons() {
        return theoreticalComparisons;
    }

    public void setTheoreticalComparisons(Integer theoreticalComparisons) {
        this.theoreticalComparisons = theoreticalComparisons;
    }

    public Integer getActualComparisons() {
        return actualComparisons;
    }

    public void setActualComparisons(Integer actualComparisons) {
        this.actualComparisons = actualComparisons;
    }

    public Boolean getComparisonMatch() {
        return comparisonMatch;
    }

    public void setComparisonMatch(Boolean comparisonMatch) {
        this.comparisonMatch = comparisonMatch;
    }

    public Boolean getHasFixedComparisonCount() {
        return hasFixedComparisonCount;
    }

    public void setHasFixedComparisonCount(Boolean hasFixedComparisonCount) {
        this.hasFixedComparisonCount = hasFixedComparisonCount;
    }

    public Boolean getEarlyStoppingUsed() {
        return earlyStoppingUsed;
    }

    public void setEarlyStoppingUsed(Boolean earlyStoppingUsed) {
        this.earlyStoppingUsed = earlyStoppingUsed;
    }

    public Boolean getComparedWithOtherAlgorithm() {
        return comparedWithOtherAlgorithm;
    }

    public void setComparedWithOtherAlgorithm(Boolean comparedWithOtherAlgorithm) {
        this.comparedWithOtherAlgorithm = comparedWithOtherAlgorithm;
    }

    public List<Map<String, Object>> getOtherAlgorithmStats() {
        return otherAlgorithmStats;
    }

    public void setOtherAlgorithmStats(List<Map<String, Object>> otherAlgorithmStats) {
        this.otherAlgorithmStats = otherAlgorithmStats;
    }
}
