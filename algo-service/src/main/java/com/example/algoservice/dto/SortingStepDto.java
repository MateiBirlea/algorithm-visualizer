package com.example.algoservice.dto;

import java.util.List;

public class SortingStepDto {
    private int stepIndex;
    private Integer totalSteps;
    private int stageIndex;
    private int leftIndex;
    private int rightIndex;
    private int leftValue;
    private int rightValue;
    private boolean swapped;
    private List<Integer> arrayState;
    private List<Integer> comparedIndices;
    private List<Integer> comparedValuesBefore;
    private List<Integer> comparedValuesAfter;
    private Boolean didSwap;
    private List<Integer> arrayBeforeStep;
    private List<Integer> arrayAfterStep;
    private String explanation;
    private String comparatorDirection;
    private String phaseName;
    private int comparatorDistance;
    private Boolean isFinalStep;
    private Boolean isArrayGloballySortedAfterStep;
    private Integer bitonicSequenceSize;
    private Integer mergeSize;
    private Integer networkStage;
    private Integer networkSubStage;
    private Boolean isBuildingBitonicSequence;
    private Boolean isBitonicMergeStep;
    private String oddEvenPhase;
    private Integer passNumber;

    public int getStepIndex() {
        return stepIndex;
    }

    public void setStepIndex(int stepIndex) {
        this.stepIndex = stepIndex;
    }

    public Integer getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(Integer totalSteps) {
        this.totalSteps = totalSteps;
    }

    public int getStageIndex() {
        return stageIndex;
    }

    public void setStageIndex(int stageIndex) {
        this.stageIndex = stageIndex;
    }

    public int getLeftIndex() {
        return leftIndex;
    }

    public void setLeftIndex(int leftIndex) {
        this.leftIndex = leftIndex;
    }

    public int getRightIndex() {
        return rightIndex;
    }

    public void setRightIndex(int rightIndex) {
        this.rightIndex = rightIndex;
    }

    public int getLeftValue() {
        return leftValue;
    }

    public void setLeftValue(int leftValue) {
        this.leftValue = leftValue;
    }

    public int getRightValue() {
        return rightValue;
    }

    public void setRightValue(int rightValue) {
        this.rightValue = rightValue;
    }

    public boolean isSwapped() {
        return swapped;
    }

    public void setSwapped(boolean swapped) {
        this.swapped = swapped;
    }

    public List<Integer> getArrayState() {
        return arrayState;
    }

    public void setArrayState(List<Integer> arrayState) {
        this.arrayState = arrayState;
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

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getComparatorDirection() {
        return comparatorDirection;
    }

    public void setComparatorDirection(String comparatorDirection) {
        this.comparatorDirection = comparatorDirection;
    }

    public String getPhaseName() {
        return phaseName;
    }

    public void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }

    public int getComparatorDistance() {
        return comparatorDistance;
    }

    public void setComparatorDistance(int comparatorDistance) {
        this.comparatorDistance = comparatorDistance;
    }

    public Boolean getIsFinalStep() {
        return isFinalStep;
    }

    public void setIsFinalStep(Boolean isFinalStep) {
        this.isFinalStep = isFinalStep;
    }

    public Boolean getIsArrayGloballySortedAfterStep() {
        return isArrayGloballySortedAfterStep;
    }

    public void setIsArrayGloballySortedAfterStep(Boolean isArrayGloballySortedAfterStep) {
        this.isArrayGloballySortedAfterStep = isArrayGloballySortedAfterStep;
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
}
