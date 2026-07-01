package com.example.explanationengine.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class SingleAlgorithmAnalysisRequestDto {

    @NotBlank
    private String source;

    @NotBlank
    private String sortDirection;

    @NotNull
    private Integer elementCount;

    @NotNull
    private List<Integer> inputArray;

    @Valid
    @NotNull
    private RunAnalysisResultDto result;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public Integer getElementCount() {
        return elementCount;
    }

    public void setElementCount(Integer elementCount) {
        this.elementCount = elementCount;
    }

    public List<Integer> getInputArray() {
        return inputArray;
    }

    public void setInputArray(List<Integer> inputArray) {
        this.inputArray = inputArray;
    }

    public RunAnalysisResultDto getResult() {
        return result;
    }

    public void setResult(RunAnalysisResultDto result) {
        this.result = result;
    }
}
