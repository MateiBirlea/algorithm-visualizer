package com.example.explanationengine.web;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.explanationengine.dto.ExplanationRequest;
import com.example.explanationengine.dto.ExplanationResponse;
import com.example.explanationengine.dto.ClassroomAnalysisRequest;
import com.example.explanationengine.dto.ComparativeAnalysisRequestDto;
import com.example.explanationengine.dto.RunAnalysisRequest;
import com.example.explanationengine.dto.SingleAlgorithmAnalysisRequestDto;
import com.example.explanationengine.service.ExplanationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/explanations")
@Validated
@CrossOrigin(origins = "http://localhost:3000")
public class ExplanationController {

    private final ExplanationService explanationService;

    public ExplanationController(ExplanationService explanationService) {
        this.explanationService = explanationService;
    }

    @PostMapping
    public ResponseEntity<ExplanationResponse> generate(@Valid @RequestBody ExplanationRequest request) {
        return ResponseEntity.ok(explanationService.generate(request));
    }

    @PostMapping("/run-analysis")
    public ResponseEntity<ExplanationResponse> analyzeRun(@Valid @RequestBody RunAnalysisRequest request) {
        return ResponseEntity.ok(explanationService.analyzeRun(request));
    }

    @PostMapping("/run-analysis/single")
    public ResponseEntity<ExplanationResponse> analyzeSingleRun(@Valid @RequestBody SingleAlgorithmAnalysisRequestDto request) {
        return ResponseEntity.ok(explanationService.analyzeSingleAlgorithm(request));
    }

    @PostMapping("/run-analysis/comparative")
    public ResponseEntity<ExplanationResponse> analyzeComparativeRun(@Valid @RequestBody ComparativeAnalysisRequestDto request) {
        return ResponseEntity.ok(explanationService.analyzeComparative(request));
    }

    @PostMapping("/classroom-analysis")
    public ResponseEntity<ExplanationResponse> analyzeClassroom(@RequestBody ClassroomAnalysisRequest request) {
        return ResponseEntity.ok(explanationService.analyzeClassroom(request));
    }
}
