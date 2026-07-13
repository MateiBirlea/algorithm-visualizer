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
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class AlgoServiceApplicationTests {

    @Test
    void contextLoads() {
    }

    @ParameterizedTest
    @MethodSource("validBitonicCases")
    void bitonicRunsOnlyForPowerOfTwoSizes(int size, SortDirection direction) {
        SortingNetworkExecuteRequestDto request = requestFor(values(size), SortingNetworkAlgorithm.BITONIC, direction);

        SortingNetworkExecuteResponseDto response = new SortingNetworkServiceImpl().execute(request);

        assertThat(response.getRequestedAlgorithm()).isEqualTo(SortingNetworkAlgorithm.BITONIC);
        assertThat(response.getEffectiveAlgorithm()).isEqualTo(SortingNetworkAlgorithm.BITONIC);
        assertThat(response.getExecutedAs()).isEqualTo(SortingNetworkAlgorithm.BITONIC);
        assertThat(response.getCorrectlySorted()).isTrue();
        assertThat(response.getFinalArray()).isEqualTo(sorted(values(size), direction));
        assertThat(response.getSteps())
                .allSatisfy(step -> assertThat(step.getPhaseName()).isIn("BUILD_BITONIC_SEQUENCE", "BITONIC_MERGE"));
    }

    @ParameterizedTest
    @MethodSource("invalidBitonicSizes")
    void bitonicRejectsNonPowerOfTwoSizesWithoutFallback(int size) {
        SortingNetworkExecuteRequestDto request = requestFor(values(size), SortingNetworkAlgorithm.BITONIC, SortDirection.ASC);

        assertThatThrownBy(() -> new SortingNetworkServiceImpl().execute(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Bitonic Sort poate fi executat numai");
    }

    @Test
    void bitonicForEightElementsContainsExpectedFinalMergeComparators() {
        SortingNetworkExecuteRequestDto request = requestFor(
                List.of(7, 2, 9, 1, 5, 3, 8, 4),
                SortingNetworkAlgorithm.BITONIC,
                SortDirection.ASC
        );

        SortingNetworkExecuteResponseDto response = new SortingNetworkServiceImpl().execute(request);
        List<String> bitonicMergePairs = response.getSteps().stream()
                .filter(step -> "BITONIC_MERGE".equals(step.getPhaseName()))
                .map(step -> step.getLeftIndex() + "-" + step.getRightIndex())
                .toList();

        assertThat(bitonicMergePairs).containsSubsequence("0-4", "1-5", "2-6", "3-7");
        assertThat(bitonicMergePairs).contains("0-2", "1-3", "4-6", "5-7", "0-1", "2-3", "4-5", "6-7");
        assertThat(response.getFinalArray()).isEqualTo(List.of(1, 2, 3, 4, 5, 7, 8, 9));
        assertThat(response.getSteps())
                .allSatisfy(step -> {
                    assertThat(step.getLeftIndex()).isBetween(0, 7);
                    assertThat(step.getRightIndex()).isBetween(0, 7);
                    assertThat(step.getComparatorDirection()).isIn("ASC", "DESC");
                    assertThat(step.getPhaseName()).isNotBlank();
                });
    }

    @ParameterizedTest
    @MethodSource("sortingCases")
    void allAlgorithmsSortCorrectlyForRequiredCases(List<Integer> values, SortDirection direction) {
        SortingNetworkServiceImpl service = new SortingNetworkServiceImpl();
        for (SortingNetworkAlgorithm algorithm : SortingNetworkAlgorithm.values()) {
            if (algorithm == SortingNetworkAlgorithm.BITONIC && !isPowerOfTwo(values.size())) {
                continue;
            }
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
                .filter(algorithm -> algorithm != SortingNetworkAlgorithm.BITONIC || isPowerOfTwo(values.size()))
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
        request.setValues(IntStream.rangeClosed(1, 64).boxed().toList());
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

    private static Stream<org.junit.jupiter.params.provider.Arguments> validBitonicCases() {
        return Stream.of(2, 4, 8, 16)
                .flatMap(size -> Stream.of(
                        org.junit.jupiter.params.provider.Arguments.of(size, SortDirection.ASC),
                        org.junit.jupiter.params.provider.Arguments.of(size, SortDirection.DESC)
                ));
    }

    private static Stream<Integer> invalidBitonicSizes() {
        return Stream.of(3, 5, 6, 7, 9, 10, 15, 17);
    }

    private static List<Integer> values(int size) {
        return IntStream.range(0, size)
                .map(i -> (i * 37 + 11) % 101)
                .boxed()
                .toList();
    }

    private boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }

    private List<Integer> sorted(List<Integer> values, SortDirection direction) {
        Comparator<Integer> comparator = direction == SortDirection.ASC
                ? Comparator.naturalOrder()
                : Comparator.reverseOrder();
        return values.stream().sorted(comparator).toList();
    }
}
