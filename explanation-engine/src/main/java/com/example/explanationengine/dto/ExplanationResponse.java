package com.example.explanationengine.dto;

public class ExplanationResponse {
    private String explanation;
    private String model;
    private long latencyMs;

    public ExplanationResponse() {
    }

    public ExplanationResponse(String explanation, String model, long latencyMs) {
        this.explanation = explanation;
        this.model = model;
        this.latencyMs = latencyMs;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(long latencyMs) {
        this.latencyMs = latencyMs;
    }
}
