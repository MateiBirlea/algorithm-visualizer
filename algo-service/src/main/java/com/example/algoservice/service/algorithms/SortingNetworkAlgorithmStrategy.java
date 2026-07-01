package com.example.algoservice.service.algorithms;

import com.example.algoservice.dto.SortingStepDto;
import com.example.algoservice.model.SortDirection;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public interface SortingNetworkAlgorithmStrategy {
    void execute(List<Integer> values, SortDirection direction, List<SortingStepDto> steps, AtomicInteger stepCounter);
}
