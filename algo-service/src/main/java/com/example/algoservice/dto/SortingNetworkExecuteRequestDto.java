package com.example.algoservice.dto;

import com.example.algoservice.model.SortDirection;
import com.example.algoservice.model.SortingNetworkAlgorithm;

import java.util.List;

public class SortingNetworkExecuteRequestDto {
    private List<Integer> values;
    private SortingNetworkAlgorithm algorithm;
    private SortDirection direction;
    private Boolean forceRequestedAlgorithm;

    public List<Integer> getValues() {
        return values;
    }

    public void setValues(List<Integer> values) {
        this.values = values;
    }

    public SortingNetworkAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(SortingNetworkAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public SortDirection getDirection() {
        return direction;
    }

    public void setDirection(SortDirection direction) {
        this.direction = direction;
    }

    public Boolean getForceRequestedAlgorithm() {
        return forceRequestedAlgorithm;
    }

    public void setForceRequestedAlgorithm(Boolean forceRequestedAlgorithm) {
        this.forceRequestedAlgorithm = forceRequestedAlgorithm;
    }
}
