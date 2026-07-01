package com.example.algoservice.service.algorithms;

import com.example.algoservice.dto.SortingStepDto;
import com.example.algoservice.model.SortDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

abstract class AbstractSortingNetworkAlgorithm implements SortingNetworkAlgorithmStrategy {

    protected void compareAndRecord(
            List<Integer> values,
            int i,
            int j,
            SortDirection direction,
            int stageIndex,
            int subStageIndex,
            String phaseName,
            Integer mergeSize,
            Integer passNumber,
            String explanationPrefix,
            List<SortingStepDto> steps,
            AtomicInteger stepCounter
    ) {
        List<Integer> before = new ArrayList<>(values);
        int left = values.get(i);
        int right = values.get(j);
        boolean ascending = direction == SortDirection.ASC;
        boolean shouldSwap = ascending ? left > right : left < right;

        if (shouldSwap) {
            values.set(i, right);
            values.set(j, left);
        }

        List<Integer> after = new ArrayList<>(values);
        SortingStepDto step = new SortingStepDto();
        step.setStepIndex(stepCounter.getAndIncrement());
        step.setStageIndex(stageIndex);
        step.setLeftIndex(i);
        step.setRightIndex(j);
        step.setLeftValue(left);
        step.setRightValue(right);
        step.setSwapped(shouldSwap);
        step.setDidSwap(shouldSwap);
        step.setArrayState(after);
        step.setArrayBeforeStep(before);
        step.setArrayAfterStep(after);
        step.setComparedIndices(List.of(i, j));
        step.setComparedValuesBefore(List.of(left, right));
        step.setComparedValuesAfter(List.of(after.get(i), after.get(j)));
        step.setComparatorDirection(ascending ? "ASC" : "DESC");
        step.setComparatorDistance(j - i);
        step.setPhaseName(phaseName);
        step.setMergeSize(mergeSize);
        step.setNetworkStage(stageIndex);
        step.setNetworkSubStage(subStageIndex);
        step.setPassNumber(passNumber);
        step.setExplanation(explanationPrefix + ": compara " + i + " cu " + j + ".");
        steps.add(step);
    }
}
