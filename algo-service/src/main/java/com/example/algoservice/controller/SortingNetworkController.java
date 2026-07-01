package com.example.algoservice.controller;

import com.example.algoservice.dto.SortingNetworkExecuteRequestDto;
import com.example.algoservice.dto.SortingNetworkExecuteResponseDto;
import com.example.algoservice.model.SortingNetworkAlgorithm;
import com.example.algoservice.service.SortingNetworkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sorting-networks")
@CrossOrigin(origins = "http://localhost:3000")
public class SortingNetworkController {

    private final SortingNetworkService sortingNetworkService;

    public SortingNetworkController(SortingNetworkService sortingNetworkService) {
        this.sortingNetworkService = sortingNetworkService;
    }

    @PostMapping("/execute")
    public ResponseEntity<SortingNetworkExecuteResponseDto> execute(@RequestBody SortingNetworkExecuteRequestDto request) {
        return ResponseEntity.ok(sortingNetworkService.execute(request));
    }

    @GetMapping("/algorithms")
    public ResponseEntity<List<Map<String, String>>> getAlgorithms() {
        List<Map<String, String>> algorithms = List.of(
                Map.of("code", SortingNetworkAlgorithm.BITONIC.name(), "name", "Bitonic Sorting Network"),
                Map.of("code", SortingNetworkAlgorithm.ODD_EVEN.name(), "name", "Odd-Even Sorting Network"),
                Map.of("code", SortingNetworkAlgorithm.BATCHER_ODD_EVEN_MERGE_SORT.name(), "name", "Batcher Odd-Even Merge Sort"),
                Map.of("code", SortingNetworkAlgorithm.PAIRWISE_SORTING_NETWORK.name(), "name", "Pairwise Sorting Network"),
                Map.of("code", SortingNetworkAlgorithm.BUBBLE_SORTING_NETWORK.name(), "name", "Bubble Sorting Network")
        );
        return ResponseEntity.ok(algorithms);
    }

    @GetMapping("/examples")
    public ResponseEntity<List<Map<String, Object>>> getExamples() {
        List<Map<String, Object>> examples = List.of(
                Map.of("name", "Random 8", "values", List.of(7, 2, 9, 1, 5, 3, 8, 4)),
                Map.of("name", "Reverse 8", "values", List.of(8, 7, 6, 5, 4, 3, 2, 1)),
                Map.of("name", "Sorted 8", "values", List.of(1, 2, 3, 4, 5, 6, 7, 8))
        );
        return ResponseEntity.ok(examples);
    }
}
