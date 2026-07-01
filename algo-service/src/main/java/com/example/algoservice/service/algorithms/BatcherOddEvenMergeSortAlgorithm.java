package com.example.algoservice.service.algorithms;

import com.example.algoservice.dto.SortingStepDto;
import com.example.algoservice.model.SortDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BatcherOddEvenMergeSortAlgorithm extends AbstractSortingNetworkAlgorithm {

    @Override
    public void execute(List<Integer> values, SortDirection direction, List<SortingStepDto> steps, AtomicInteger stepCounter) {
        int originalSize = values.size();
        int paddedSize = nextPowerOfTwo(originalSize);
        int sentinel = direction == SortDirection.ASC ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        while (values.size() < paddedSize) {
            values.add(sentinel);
        }

        oddEvenMergeSort(values, 0, values.size(), direction, 0, originalSize, steps, stepCounter);

        while (values.size() > originalSize) {
            values.remove(values.size() - 1);
        }
    }

    private void oddEvenMergeSort(List<Integer> values, int low, int count, SortDirection direction, int stage,
                                  int originalSize, List<SortingStepDto> steps, AtomicInteger stepCounter) {
        if (count <= 1) {
            return;
        }

        int mid = count / 2;
        oddEvenMergeSort(values, low, mid, direction, stage + 1, originalSize, steps, stepCounter);
        oddEvenMergeSort(values, low + mid, count - mid, direction, stage + 1, originalSize, steps, stepCounter);
        oddEvenMerge(values, low, count, 1, direction, stage, count, originalSize, steps, stepCounter);
    }

    private void oddEvenMerge(List<Integer> values, int low, int count, int distance, SortDirection direction,
                              int stage, int mergeSize, int originalSize, List<SortingStepDto> steps, AtomicInteger stepCounter) {
        int doubleDistance = distance * 2;
        if (doubleDistance < count) {
            oddEvenMerge(values, low, count, doubleDistance, direction, stage + 1, mergeSize, originalSize, steps, stepCounter);
            oddEvenMerge(values, low + distance, count, doubleDistance, direction, stage + 1, mergeSize, originalSize, steps, stepCounter);
            for (int i = low + distance; i + distance < low + count && i + distance < values.size(); i += doubleDistance) {
                compareAndMaybeRecord(values, i, i + distance, direction, stage, distance,
                        "ODD_EVEN_MERGE", mergeSize, originalSize, "Batcher Odd-Even Merge", steps, stepCounter);
            }
        } else if (low + distance < values.size()) {
            compareAndMaybeRecord(values, low, low + distance, direction, stage, distance,
                    "ODD_EVEN_MERGE_SORT", mergeSize, originalSize, "Batcher Odd-Even Merge Sort", steps, stepCounter);
        }
    }

    private void compareAndMaybeRecord(List<Integer> values, int i, int j, SortDirection direction, int stage, int distance,
                                       String phaseName, int mergeSize, int originalSize, String explanationPrefix,
                                       List<SortingStepDto> steps, AtomicInteger stepCounter) {
        if (i >= originalSize || j >= originalSize) {
            compareOnly(values, i, j, direction);
            return;
        }

        List<Integer> before = new ArrayList<>(values.subList(0, originalSize));
        compareAndRecord(values, i, j, direction, stage, distance, phaseName, mergeSize, null,
                explanationPrefix, steps, stepCounter);
        SortingStepDto step = steps.get(steps.size() - 1);
        step.setArrayBeforeStep(before);
        step.setArrayAfterStep(new ArrayList<>(values.subList(0, originalSize)));
        step.setArrayState(new ArrayList<>(values.subList(0, originalSize)));
    }

    private void compareOnly(List<Integer> values, int i, int j, SortDirection direction) {
        int left = values.get(i);
        int right = values.get(j);
        boolean shouldSwap = direction == SortDirection.ASC ? left > right : left < right;
        if (shouldSwap) {
            values.set(i, right);
            values.set(j, left);
        }
    }

    private int nextPowerOfTwo(int n) {
        int p = 1;
        while (p < n) {
            p <<= 1;
        }
        return p;
    }
}
