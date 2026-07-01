package com.example.algoservice.service.algorithms;

import com.example.algoservice.dto.SortingStepDto;
import com.example.algoservice.model.SortDirection;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BubbleSortingNetworkAlgorithm extends AbstractSortingNetworkAlgorithm {

    @Override
    public void execute(List<Integer> values, SortDirection direction, List<SortingStepDto> steps, AtomicInteger stepCounter) {
        int n = values.size();
        for (int pass = 0; pass < n - 1; pass++) {
            for (int i = 0; i < n - pass - 1; i++) {
                compareAndRecord(values, i, i + 1, direction, pass, i,
                        "BUBBLE_PASS", n - pass, pass + 1, "Bubble pass", steps, stepCounter);
            }
        }
    }
}
