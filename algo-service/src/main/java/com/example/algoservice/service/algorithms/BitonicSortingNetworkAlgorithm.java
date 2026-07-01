package com.example.algoservice.service.algorithms;

import com.example.algoservice.dto.SortingStepDto;
import com.example.algoservice.model.SortDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BitonicSortingNetworkAlgorithm implements SortingNetworkAlgorithmStrategy {

    @Override
    public void execute(List<Integer> values, SortDirection direction, List<SortingStepDto> steps, AtomicInteger stepCounter) {
        boolean ascending = direction == SortDirection.ASC;
        int originalSize = values.size();
        int paddedSize = nextPowerOfTwo(originalSize);
        int sentinel = ascending ? Integer.MAX_VALUE : Integer.MIN_VALUE;

        while (values.size() < paddedSize) {
            values.add(sentinel);
        }

        bitonicSort(values, 0, values.size(), ascending, 0, steps, stepCounter, originalSize, values.size());

        while (values.size() > originalSize) {
            values.remove(values.size() - 1);
        }
    }

    private void bitonicSort(List<Integer> arr, int low, int count, boolean ascending, int stageIndex,
                             List<SortingStepDto> steps, AtomicInteger stepCounter, int originalSize, int paddedSize) {
        if (count <= 1) {
            return;
        }

        int k = count / 2;
        bitonicSort(arr, low, k, true, stageIndex + 1, steps, stepCounter, originalSize, paddedSize);
        bitonicSort(arr, low + k, k, false, stageIndex + 1, steps, stepCounter, originalSize, paddedSize);
        String phaseName = count == paddedSize ? "BITONIC_MERGE" : "BUILD_BITONIC_SEQUENCE";
        bitonicMerge(arr, low, count, ascending, stageIndex, steps, stepCounter, originalSize, phaseName);
    }

    private void bitonicMerge(List<Integer> arr, int low, int count, boolean ascending, int stageIndex,
                              List<SortingStepDto> steps, AtomicInteger stepCounter, int originalSize, String phaseName) {
        if (count <= 1) {
            return;
        }

        int k = count / 2;
        for (int i = low; i < low + k; i++) {
            compareAndSwap(arr, i, i + k, ascending, stageIndex, steps, stepCounter, originalSize, phaseName, count);
        }
        bitonicMerge(arr, low, k, ascending, stageIndex + 1, steps, stepCounter, originalSize, phaseName);
        bitonicMerge(arr, low + k, k, ascending, stageIndex + 1, steps, stepCounter, originalSize, phaseName);
    }

    private void compareAndSwap(List<Integer> arr, int i, int j, boolean ascending, int stageIndex,
                                List<SortingStepDto> steps, AtomicInteger stepCounter, int originalSize, String phaseName, int mergeSize) {
        int left = arr.get(i);
        int right = arr.get(j);
        boolean shouldSwap = ascending ? left > right : left < right;
        if (shouldSwap) {
            arr.set(i, right);
            arr.set(j, left);
        }

        if (i >= originalSize || j >= originalSize) {
            return;
        }

        SortingStepDto step = new SortingStepDto();
        step.setStepIndex(stepCounter.getAndIncrement());
        step.setStageIndex(stageIndex);
        step.setLeftIndex(i);
        step.setRightIndex(j);
        step.setLeftValue(left);
        step.setRightValue(right);
        step.setSwapped(shouldSwap);
        step.setArrayState(new ArrayList<>(arr.subList(0, originalSize)));
        step.setComparatorDirection(ascending ? "ASC" : "DESC");
        step.setPhaseName(phaseName);
        step.setComparatorDistance(j - i);
        step.setBitonicSequenceSize(mergeSize);
        step.setMergeSize(mergeSize);
        step.setNetworkStage(stageIndex);
        step.setNetworkSubStage(j - i);
        step.setIsBuildingBitonicSequence("BUILD_BITONIC_SEQUENCE".equals(phaseName));
        step.setIsBitonicMergeStep("BITONIC_MERGE".equals(phaseName));
        step.setExplanation("Compara pozitiile " + i + " si " + j + ".");
        steps.add(step);
    }

    private int nextPowerOfTwo(int n) {
        int p = 1;
        while (p < n) {
            p <<= 1;
        }
        return p;
    }
}
