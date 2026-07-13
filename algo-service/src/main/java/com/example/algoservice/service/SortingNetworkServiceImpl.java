package com.example.algoservice.service;

import com.example.algoservice.dto.SortingMetricsDto;
import com.example.algoservice.dto.SortingNetworkExecuteRequestDto;
import com.example.algoservice.dto.SortingNetworkExecuteResponseDto;
import com.example.algoservice.dto.SortingStepDto;
import com.example.algoservice.model.SortDirection;
import com.example.algoservice.model.SortingNetworkAlgorithm;
import com.example.algoservice.service.algorithms.BitonicSortingNetworkAlgorithm;
import com.example.algoservice.service.algorithms.BatcherOddEvenMergeSortAlgorithm;
import com.example.algoservice.service.algorithms.BubbleSortingNetworkAlgorithm;
import com.example.algoservice.service.algorithms.OddEvenSortingNetworkAlgorithm;
import com.example.algoservice.service.algorithms.PairwiseSortingNetworkAlgorithm;
import com.example.algoservice.service.algorithms.SortingNetworkAlgorithmStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SortingNetworkServiceImpl implements SortingNetworkService {
    private static final int MIN_VALUES = 2;
    private static final int MAX_VALUES = 100;
    private static final String BITONIC_POWER_OF_TWO_MESSAGE =
            "Bitonic Sort poate fi executat numai pentru un numar de elemente egal cu o putere a lui 2: 2, 4, 8, 16, 32 sau 64.";

    private final Map<SortingNetworkAlgorithm, SortingNetworkAlgorithmStrategy> strategies = Map.of(
            SortingNetworkAlgorithm.BITONIC, new BitonicSortingNetworkAlgorithm(),
            SortingNetworkAlgorithm.ODD_EVEN, new OddEvenSortingNetworkAlgorithm(),
            SortingNetworkAlgorithm.BATCHER_ODD_EVEN_MERGE_SORT, new BatcherOddEvenMergeSortAlgorithm(),
            SortingNetworkAlgorithm.PAIRWISE_SORTING_NETWORK, new PairwiseSortingNetworkAlgorithm(),
            SortingNetworkAlgorithm.BUBBLE_SORTING_NETWORK, new BubbleSortingNetworkAlgorithm()
    );

    @Override
    public SortingNetworkExecuteResponseDto execute(SortingNetworkExecuteRequestDto request) {
        validate(request);

        List<Integer> working = new ArrayList<>(request.getValues());
        List<SortingStepDto> steps = new ArrayList<>();
        AtomicInteger stepCounter = new AtomicInteger(0);
        SortingNetworkAlgorithm effectiveAlgorithm = request.getAlgorithm();

        long start = System.nanoTime();
        SortingNetworkAlgorithmStrategy strategy = strategies.get(effectiveAlgorithm);
        strategy.execute(working, request.getDirection(), steps, stepCounter);
        long end = System.nanoTime();
        enrichSteps(steps, request.getDirection());
        long executionTimeMs = TimeUnit.NANOSECONDS.toMillis(end - start);
        if (executionTimeMs <= 0 && !steps.isEmpty()) {
            executionTimeMs = 1;
        }

        SortingMetricsDto metrics = new SortingMetricsDto();
        metrics.setTotalSteps(steps.size());
        metrics.setTotalComparisons(steps.size());
        metrics.setTotalSwaps((int) steps.stream().filter(SortingStepDto::isSwapped).count());
        metrics.setExecutionTimeMs(executionTimeMs);

        SortingNetworkExecuteResponseDto response = new SortingNetworkExecuteResponseDto();
        response.setInitialValues(new ArrayList<>(request.getValues()));
        response.setSteps(steps);
        response.setMetrics(metrics);
        response.setRequestedAlgorithm(request.getAlgorithm());
        response.setEffectiveAlgorithm(effectiveAlgorithm);
        response.setAlgorithmName(request.getAlgorithm());
        response.setExecutedAs(effectiveAlgorithm);
        response.setFinalArray(new ArrayList<>(working));
        response.setTotalSteps(metrics.getTotalSteps());
        response.setTotalComparisons(metrics.getTotalComparisons());
        response.setTotalSwaps(metrics.getTotalSwaps());
        response.setExecutionTimeMs(metrics.getExecutionTimeMs());
        response.setCorrectlySorted(isSorted(working, request.getDirection()));
        return response;
    }

    private void validate(SortingNetworkExecuteRequestDto request) {
        if (request.getValues() == null || request.getValues().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lista de valori este obligatorie.");
        }
        int n = request.getValues().size();
        if (n < MIN_VALUES || n > MAX_VALUES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lista trebuie să conțină între 2 și 100 valori.");
        }
        if (request.getAlgorithm() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Algoritmul este obligatoriu.");
        }
        if (request.getAlgorithm() == SortingNetworkAlgorithm.BITONIC && !isPowerOfTwo(n)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, BITONIC_POWER_OF_TWO_MESSAGE);
        }
        if (request.getDirection() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Direcția este obligatorie.");
        }
    }

    private boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }

    private void enrichSteps(List<SortingStepDto> steps, SortDirection direction) {
        int totalSteps = steps.size();
        for (int index = 0; index < totalSteps; index++) {
            SortingStepDto step = steps.get(index);
            step.setTotalSteps(totalSteps);
            step.setIsFinalStep(index == totalSteps - 1);
            step.setDidSwap(step.getDidSwap() == null ? step.isSwapped() : step.getDidSwap());
            step.setArrayAfterStep(step.getArrayAfterStep() == null ? step.getArrayState() : step.getArrayAfterStep());
            step.setComparedIndices(step.getComparedIndices() == null ? List.of(step.getLeftIndex(), step.getRightIndex()) : step.getComparedIndices());
            step.setComparedValuesBefore(step.getComparedValuesBefore() == null ? List.of(step.getLeftValue(), step.getRightValue()) : step.getComparedValuesBefore());
            if (step.getArrayAfterStep() != null && step.getComparedValuesAfter() == null) {
                step.setComparedValuesAfter(List.of(
                        step.getArrayAfterStep().get(step.getLeftIndex()),
                        step.getArrayAfterStep().get(step.getRightIndex())
                ));
            }
            step.setIsArrayGloballySortedAfterStep(
                    step.getArrayAfterStep() != null && isSorted(step.getArrayAfterStep(), direction)
            );
        }
    }

    private boolean isSorted(List<Integer> values, SortDirection direction) {
        for (int i = 1; i < values.size(); i++) {
            if (direction == SortDirection.ASC && values.get(i - 1) > values.get(i)) {
                return false;
            }
            if (direction == SortDirection.DESC && values.get(i - 1) < values.get(i)) {
                return false;
            }
        }
        return true;
    }
}

