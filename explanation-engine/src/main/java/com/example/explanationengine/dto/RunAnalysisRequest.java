package com.example.explanationengine.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class RunAnalysisRequest {

    @NotBlank
    private String mode;

    @NotBlank
    private String source;

    @NotBlank
    private String direction;

    @NotNull
    private Integer valueCount;

    @NotNull
    private List<Integer> inputValues;

    @NotEmpty
    private List<RunAnalysisResultDto> results;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Integer getValueCount() {
        return valueCount;
    }

    public void setValueCount(Integer valueCount) {
        this.valueCount = valueCount;
    }

    public List<Integer> getInputValues() {
        return inputValues;
    }

    public void setInputValues(List<Integer> inputValues) {
        this.inputValues = inputValues;
    }

    public List<RunAnalysisResultDto> getResults() {
        return results;
    }

    public void setResults(List<RunAnalysisResultDto> results) {
        this.results = results;
    }
}

