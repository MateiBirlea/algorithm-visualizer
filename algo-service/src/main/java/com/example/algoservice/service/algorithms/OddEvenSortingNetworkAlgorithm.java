package com.example.algoservice.service.algorithms;

import com.example.algoservice.dto.SortingStepDto;
import com.example.algoservice.model.SortDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class OddEvenSortingNetworkAlgorithm implements SortingNetworkAlgorithmStrategy {

    @Override
    public void execute(List<Integer> values, SortDirection direction, List<SortingStepDto> steps, AtomicInteger stepCounter) {
        boolean ascending = direction == SortDirection.ASC;
        int n = values.size();

        for (int stage = 0; stage < n; stage++) {
            int start = stage % 2 == 0 ? 0 : 1;
            String phaseName = stage % 2 == 0 ? "EVEN_PHASE" : "ODD_PHASE";
            String oddEvenPhase = stage % 2 == 0 ? "EVEN" : "ODD";
            for (int i = start; i < n - 1; i += 2) {
                compareAndSwap(values, i, i + 1, ascending, stage, phaseName, oddEvenPhase, steps, stepCounter);
            }
        }
    }

    private void compareAndSwap(List<Integer> arr, int i, int j, boolean ascending, int stageIndex, String phaseName, String oddEvenPhase,
                                List<SortingStepDto> steps, AtomicInteger stepCounter) {
        int left = arr.get(i);
        int right = arr.get(j);
        boolean shouldSwap = ascending ? left > right : left < right;
        if (shouldSwap) {
            arr.set(i, right);
            arr.set(j, left);
        }

        SortingStepDto step = new SortingStepDto();
        step.setStepIndex(stepCounter.getAndIncrement());
        step.setStageIndex(stageIndex);
        step.setLeftIndex(i);
        step.setRightIndex(j);
        step.setLeftValue(left);
        step.setRightValue(right);
        step.setSwapped(shouldSwap);
        step.setArrayState(new ArrayList<>(arr));
        step.setComparatorDirection(ascending ? "ASC" : "DESC");
        step.setPhaseName(phaseName);
        step.setComparatorDistance(j - i);
        step.setNetworkStage(stageIndex);
        step.setNetworkSubStage(startSubStage(i));
        step.setOddEvenPhase(oddEvenPhase);
        step.setPassNumber(stageIndex + 1);
        step.setExplanation("Odd-Even Merge: compara " + i + " cu " + j + ".");
        steps.add(step);
    }

    private int startSubStage(int index) {
        return index / 2;
    }
}
