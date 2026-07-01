package com.example.classroomservice.dto;

import com.example.classroomservice.model.AlgorithmType;
import com.example.classroomservice.model.ProgressStatus;
import com.example.classroomservice.model.SortDirection;

import java.time.Instant;

public class ProgressDtos {
    public record SubmitProgressRequest(
            AlgorithmType algorithm,
            SortDirection direction,
            String inputData,
            String sortedOutput,
            Integer totalSteps,
            Integer totalComparisons,
            Integer totalSwaps,
            Long executionTimeMs
    ) {}

    public record ProgressResponse(
            Long id,
            String studentId,
            Long assignmentId,
            ProgressStatus status,
            Instant startedAt,
            Instant completedAt,
            Integer runCount,
            Instant lastRunAt,
            AlgorithmType algorithm,
            SortDirection direction,
            String inputData,
            String sortedOutput,
            Integer totalSteps,
            Integer totalComparisons,
            Integer totalSwaps,
            Long executionTimeMs
    ) {}
}
