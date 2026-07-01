package com.example.explanationengine.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class ComparativeAnalysisRequestDto {

    @Valid
    @NotEmpty
    private List<AlgorithmRunResultDto> results;

    @NotNull
    private Integer elementCount;

    @NotBlank
    private String sortDirection;

    @NotNull
    private List<Integer> inputArray;

    public List<AlgorithmRunResultDto> getResults() {
        return results;
    }

    public void setResults(List<AlgorithmRunResultDto> results) {
        this.results = results;
    }

    public Integer getElementCount() {
        return elementCount;
    }

    public void setElementCount(Integer elementCount) {
        this.elementCount = elementCount;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public List<Integer> getInputArray() {
        return inputArray;
    }

    public void setInputArray(List<Integer> inputArray) {
        this.inputArray = inputArray;
    }
}
