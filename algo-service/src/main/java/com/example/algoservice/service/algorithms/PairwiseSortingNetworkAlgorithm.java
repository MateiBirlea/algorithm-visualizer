package com.example.algoservice.service.algorithms;

import com.example.algoservice.dto.SortingStepDto;
import com.example.algoservice.model.SortDirection;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PairwiseSortingNetworkAlgorithm extends AbstractSortingNetworkAlgorithm {

    @Override
    public void execute(List<Integer> values, SortDirection direction, List<SortingStepDto> steps, AtomicInteger stepCounter) {
        int n = values.size();
        int stage = 0;

        for (int i = 0; i + 1 < n; i += 2) {
            compareAndRecord(values, i, i + 1, direction, stage, i / 2,
                    "PAIRWISE_COMPARE", 2, null, "Pairwise compare", steps, stepCounter);
        }

        for (int blockSize = 4; blockSize < n * 2; blockSize *= 2) {
            stage++;
            for (int start = 0; start < n; start += blockSize) {
                int end = Math.min(start + blockSize, n);
                runGappedMerge(values, start, end, blockSize / 2, direction, stage, blockSize, steps, stepCounter);
                runAdjacentCleanup(values, start, end, direction, stage, blockSize, steps, stepCounter);
            }
        }
    }

    private void runGappedMerge(List<Integer> values, int start, int end, int initialGap, SortDirection direction,
                                int stage, int mergeSize, List<SortingStepDto> steps, AtomicInteger stepCounter) {
        for (int gap = initialGap; gap >= 1; gap /= 2) {
            for (int i = start; i + gap < end; i++) {
                compareAndRecord(values, i, i + gap, direction, stage, gap,
                        "PAIRWISE_MERGE", mergeSize, null, "Pairwise merge", steps, stepCounter);
            }
        }
    }

    private void runAdjacentCleanup(List<Integer> values, int start, int end, SortDirection direction,
                                    int stage, int mergeSize, List<SortingStepDto> steps, AtomicInteger stepCounter) {
        for (int i = start + 1; i < end; i++) {
            for (int j = i; j > start; j--) {
                compareAndRecord(values, j - 1, j, direction, stage, j - start,
                        "PAIRWISE_MERGE", mergeSize, null, "Pairwise merge cleanup", steps, stepCounter);
            }
        }
    }
}
