package com.example.explanationengine.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ExplanationRequest {

    @NotBlank
    private String algorithm;

    @NotBlank
    private String direction;

    @NotBlank
    private String stepType;

    @NotNull
    private Integer stepIndex;

    @NotNull
    private Integer stageIndex;

    @NotNull
    private Integer leftIndex;

    @NotNull
    private Integer rightIndex;

    @NotNull
    private Integer leftValue;

    @NotNull
    private Integer rightValue;

    @NotNull
    private Boolean swapped;

    @NotNull
    private List<Integer> arrayState;

    private List<Integer> beforeArrayState;

    private List<Integer> afterArrayState;

    private String algorithmName;

    private String sortDirection;

    private Integer totalSteps;

    private List<Integer> comparedIndices;

    private List<Integer> comparedValuesBefore;

    private List<Integer> comparedValuesAfter;

    private Boolean didSwap;

    private List<Integer> arrayBeforeStep;

    private List<Integer> arrayAfterStep;

    private String comparatorDirection;

    private Integer comparatorDistance;

    private String phaseName;

    private String stageName;

    private Integer bitonicSequenceSize;

    private Integer mergeSize;

    private Integer networkStage;

    private Integer networkSubStage;

    private Boolean isBuildingBitonicSequence;

    private Boolean isBitonicMergeStep;

    private String oddEvenPhase;

    private Integer passNumber;

    private Boolean isFinalStep;

    private Boolean isArrayGloballySortedBeforeStep;

    private Boolean isArrayGloballySortedAfterStep;

    private String explanationWarning;

    private List<String> explanationRules;

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getStepType() {
        return stepType;
    }

    public void setStepType(String stepType) {
        this.stepType = stepType;
    }

    public Integer getStepIndex() {
        return stepIndex;
    }

    public void setStepIndex(Integer stepIndex) {
        this.stepIndex = stepIndex;
    }

    public Integer getStageIndex() {
        return stageIndex;
    }

    public void setStageIndex(Integer stageIndex) {
        this.stageIndex = stageIndex;
    }

    public Integer getLeftIndex() {
        return leftIndex;
    }

    public void setLeftIndex(Integer leftIndex) {
        this.leftIndex = leftIndex;
    }

    public Integer getRightIndex() {
        return rightIndex;
    }

    public void setRightIndex(Integer rightIndex) {
        this.rightIndex = rightIndex;
    }

    public Integer getLeftValue() {
        return leftValue;
    }

    public void setLeftValue(Integer leftValue) {
        this.leftValue = leftValue;
    }

    public Integer getRightValue() {
        return rightValue;
    }

    public void setRightValue(Integer rightValue) {
        this.rightValue = rightValue;
    }

    public Boolean getSwapped() {
        return swapped;
    }

    public void setSwapped(Boolean swapped) {
        this.swapped = swapped;
    }

    public List<Integer> getArrayState() {
        return arrayState;
    }

    public void setArrayState(List<Integer> arrayState) {
        this.arrayState = arrayState;
    }

    public List<Integer> getBeforeArrayState() {
        return beforeArrayState;
    }

    public void setBeforeArrayState(List<Integer> beforeArrayState) {
        this.beforeArrayState = beforeArrayState;
    }

    public List<Integer> getAfterArrayState() {
        return afterArrayState;
    }

    public void setAfterArrayState(List<Integer> afterArrayState) {
        this.afterArrayState = afterArrayState;
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

    public Integer getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(Integer totalSteps) {
        this.totalSteps = totalSteps;
    }

    public List<Integer> getComparedIndices() {
        return comparedIndices;
    }

    public void setComparedIndices(List<Integer> comparedIndices) {
        this.comparedIndices = comparedIndices;
    }

    public List<Integer> getComparedValuesBefore() {
        return comparedValuesBefore;
    }

    public void setComparedValuesBefore(List<Integer> comparedValuesBefore) {
        this.comparedValuesBefore = comparedValuesBefore;
    }

    public List<Integer> getComparedValuesAfter() {
        return comparedValuesAfter;
    }

    public void setComparedValuesAfter(List<Integer> comparedValuesAfter) {
        this.comparedValuesAfter = comparedValuesAfter;
    }

    public Boolean getDidSwap() {
        return didSwap;
    }

    public void setDidSwap(Boolean didSwap) {
        this.didSwap = didSwap;
    }

    public List<Integer> getArrayBeforeStep() {
        return arrayBeforeStep;
    }

    public void setArrayBeforeStep(List<Integer> arrayBeforeStep) {
        this.arrayBeforeStep = arrayBeforeStep;
    }

    public List<Integer> getArrayAfterStep() {
        return arrayAfterStep;
    }

    public void setArrayAfterStep(List<Integer> arrayAfterStep) {
        this.arrayAfterStep = arrayAfterStep;
    }

    public String getComparatorDirection() {
        return comparatorDirection;
    }

    public void setComparatorDirection(String comparatorDirection) {
        this.comparatorDirection = comparatorDirection;
    }

    public Integer getComparatorDistance() {
        return comparatorDistance;
    }

    public void setComparatorDistance(Integer comparatorDistance) {
        this.comparatorDistance = comparatorDistance;
    }

    public String getPhaseName() {
        return phaseName;
    }

    public void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public Integer getBitonicSequenceSize() {
        return bitonicSequenceSize;
    }

    public void setBitonicSequenceSize(Integer bitonicSequenceSize) {
        this.bitonicSequenceSize = bitonicSequenceSize;
    }

    public Integer getMergeSize() {
        return mergeSize;
    }

    public void setMergeSize(Integer mergeSize) {
        this.mergeSize = mergeSize;
    }

    public Integer getNetworkStage() {
        return networkStage;
    }

    public void setNetworkStage(Integer networkStage) {
        this.networkStage = networkStage;
    }

    public Integer getNetworkSubStage() {
        return networkSubStage;
    }

    public void setNetworkSubStage(Integer networkSubStage) {
        this.networkSubStage = networkSubStage;
    }

    public Boolean getIsBuildingBitonicSequence() {
        return isBuildingBitonicSequence;
    }

    public void setIsBuildingBitonicSequence(Boolean isBuildingBitonicSequence) {
        this.isBuildingBitonicSequence = isBuildingBitonicSequence;
    }

    public Boolean getIsBitonicMergeStep() {
        return isBitonicMergeStep;
    }

    public void setIsBitonicMergeStep(Boolean isBitonicMergeStep) {
        this.isBitonicMergeStep = isBitonicMergeStep;
    }

    public String getOddEvenPhase() {
        return oddEvenPhase;
    }

    public void setOddEvenPhase(String oddEvenPhase) {
        this.oddEvenPhase = oddEvenPhase;
    }

    public Integer getPassNumber() {
        return passNumber;
    }

    public void setPassNumber(Integer passNumber) {
        this.passNumber = passNumber;
    }

    public Boolean getIsFinalStep() {
        return isFinalStep;
    }

    public void setIsFinalStep(Boolean isFinalStep) {
        this.isFinalStep = isFinalStep;
    }

    public Boolean getIsArrayGloballySortedBeforeStep() {
        return isArrayGloballySortedBeforeStep;
    }

    public void setIsArrayGloballySortedBeforeStep(Boolean isArrayGloballySortedBeforeStep) {
        this.isArrayGloballySortedBeforeStep = isArrayGloballySortedBeforeStep;
    }

    public Boolean getIsArrayGloballySortedAfterStep() {
        return isArrayGloballySortedAfterStep;
    }

    public void setIsArrayGloballySortedAfterStep(Boolean isArrayGloballySortedAfterStep) {
        this.isArrayGloballySortedAfterStep = isArrayGloballySortedAfterStep;
    }

    public String getExplanationWarning() {
        return explanationWarning;
    }

    public void setExplanationWarning(String explanationWarning) {
        this.explanationWarning = explanationWarning;
    }

    public List<String> getExplanationRules() {
        return explanationRules;
    }

    public void setExplanationRules(List<String> explanationRules) {
        this.explanationRules = explanationRules;
    }
}
