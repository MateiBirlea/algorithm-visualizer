package com.example.algoservice;

import com.example.algoservice.dto.SortingNetworkExecuteRequestDto;
import com.example.algoservice.dto.SortingNetworkExecuteResponseDto;
import com.example.algoservice.model.SortDirection;
import com.example.algoservice.model.SortingNetworkAlgorithm;
import com.example.algoservice.service.SortingNetworkServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AlgoServiceApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void keepsExistingFallbackWhenRequestedAlgorithmIsNotForced() {
        SortingNetworkExecuteRequestDto request = requestFor(SortingNetworkAlgorithm.BITONIC, false);

        SortingNetworkExecuteResponseDto response = new SortingNetworkServiceImpl().execute(request);

        assertThat(response.getRequestedAlgorithm()).isEqualTo(SortingNetworkAlgorithm.BITONIC);
        assertThat(response.getEffectiveAlgorithm()).isEqualTo(SortingNetworkAlgorithm.ODD_EVEN);
    }

    @Test
    void forcedBitonicRunUsesBitonicStepsForNonPowerOfTwoInput() {
        SortingNetworkExecuteRequestDto request = requestFor(SortingNetworkAlgorithm.BITONIC, true);

        SortingNetworkExecuteResponseDto response = new SortingNetworkServiceImpl().execute(request);

        assertThat(response.getRequestedAlgorithm()).isEqualTo(SortingNetworkAlgorithm.BITONIC);
        assertThat(response.getEffectiveAlgorithm()).isEqualTo(SortingNetworkAlgorithm.BITONIC);
        assertThat(response.getSteps()).isNotEmpty();
        assertThat(response.getSteps())
                .allSatisfy(step -> {
                    assertThat(step.getPhaseName()).isIn("BUILD_BITONIC_SEQUENCE", "BITONIC_MERGE");
                    assertThat(step.getExplanation()).doesNotContain("Odd-Even");
                });
    }

    @ParameterizedTest
    @MethodSource("sortingCases")
    void allAlgorithmsSortCorrectlyForRequiredCases(List<Integer> values, SortDirection direction) {
        SortingNetworkServiceImpl service = new SortingNetworkServiceImpl();
        for (SortingNetworkAlgorithm algorithm : SortingNetworkAlgorithm.values()) {
            SortingNetworkExecuteRequestDto request = new SortingNetworkExecuteRequestDto();
            request.setValues(new ArrayList<>(values));
            request.setAlgorithm(algorithm);
            request.setDirection(direction);
            request.setForceRequestedAlgorithm(true);

            SortingNetworkExecuteResponseDto response = service.execute(request);
            List<Integer> finalArray = response.getSteps().isEmpty()
                    ? values
                    : response.getSteps().get(response.getSteps().size() - 1).getArrayState();

            assertThat(response.getEffectiveAlgorithm()).isEqualTo(algorithm);
            assertThat(finalArray).isEqualTo(sorted(values, direction));
            assertThat(response.getMetrics().getTotalSteps()).isEqualTo(response.getSteps().size());
            assertThat(response.getMetrics().getTotalComparisons()).isEqualTo(response.getSteps().size());
        }
    }

    @Test
    void parallelAlgorithmsHaveIndependentStatistics() {
        SortingNetworkServiceImpl service = new SortingNetworkServiceImpl();
        List<Integer> values = IntStream.rangeClosed(1, 50)
                .map(i -> (i * 37) % 101)
                .boxed()
                .toList();

        List<String> signatures = Stream.of(SortingNetworkAlgorithm.values())
                .map(algorithm -> {
                    SortingNetworkExecuteRequestDto request = new SortingNetworkExecuteRequestDto();
                    request.setValues(new ArrayList<>(values));
                    request.setAlgorithm(algorithm);
                    request.setDirection(SortDirection.ASC);
                    request.setForceRequestedAlgorithm(true);
                    SortingNetworkExecuteResponseDto response = service.execute(request);
                    return response.getMetrics().getTotalSteps()
                            + "/" + response.getMetrics().getTotalComparisons()
                            + "/" + response.getMetrics().getTotalSwaps();
                })
                .distinct()
                .toList();

        assertThat(signatures).hasSizeGreaterThan(1);
    }

    @Test
    void pairwiseImplementationIsMarkedAsNaiveWhenUsingCleanupComparators() {
        SortingNetworkServiceImpl service = new SortingNetworkServiceImpl();
        List<Integer> values = IntStream.rangeClosed(1, 50)
                .map(i -> (i * 37) % 101)
                .boxed()
                .toList();

        SortingNetworkExecuteResponseDto pairwise = service.execute(requestFor(values, SortingNetworkAlgorithm.PAIRWISE_SORTING_NETWORK, SortDirection.ASC));
        SortingNetworkExecuteResponseDto oddEven = service.execute(requestFor(values, SortingNetworkAlgorithm.ODD_EVEN, SortDirection.ASC));

        assertThat(pairwise.getSteps()).isNotEmpty();
        assertThat(pairwise.getSteps())
                .allSatisfy(step -> assertThat(step.getPhaseName()).startsWith("PAIRWISE_NAIVE"));
        assertThat(pairwise.getMetrics().getTotalComparisons())
                .isGreaterThan(oddEven.getMetrics().getTotalComparisons());
    }

    private SortingNetworkExecuteRequestDto requestFor(SortingNetworkAlgorithm algorithm, boolean forceRequestedAlgorithm) {
        SortingNetworkExecuteRequestDto request = new SortingNetworkExecuteRequestDto();
        request.setValues(IntStream.rangeClosed(1, 50).boxed().toList());
        request.setAlgorithm(algorithm);
        request.setDirection(SortDirection.DESC);
        request.setForceRequestedAlgorithm(forceRequestedAlgorithm);
        return request;
    }

    private SortingNetworkExecuteRequestDto requestFor(List<Integer> values, SortingNetworkAlgorithm algorithm, SortDirection direction) {
        SortingNetworkExecuteRequestDto request = new SortingNetworkExecuteRequestDto();
        request.setValues(new ArrayList<>(values));
        request.setAlgorithm(algorithm);
        request.setDirection(direction);
        request.setForceRequestedAlgorithm(true);
        return request;
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> sortingCases() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(List.of(42, 7, 19, 3, 88, 12, 64, 27), SortDirection.ASC),
                org.junit.jupiter.params.provider.Arguments.of(List.of(42, 7, 19, 3, 88, 12, 64, 27), SortDirection.DESC),
                org.junit.jupiter.params.provider.Arguments.of(List.of(5, 2, 5, 1, 5, 3, 5, 4), SortDirection.ASC),
                org.junit.jupiter.params.provider.Arguments.of(List.of(2, 1, 4, 3, 6, 5, 8, 7), SortDirection.ASC),
                org.junit.jupiter.params.provider.Arguments.of(IntStream.rangeClosed(1, 50).map(i -> (i * 37) % 101).boxed().toList(), SortDirection.ASC)
        );
    }

    private List<Integer> sorted(List<Integer> values, SortDirection direction) {
        Comparator<Integer> comparator = direction == SortDirection.ASC
                ? Comparator.naturalOrder()
                : Comparator.reverseOrder();
        return values.stream().sorted(comparator).toList();
    }
}
