package com.example.classroomservice.dto;

import com.example.classroomservice.model.AlgorithmType;
import com.example.classroomservice.model.AssignmentStatus;
import com.example.classroomservice.model.SortDirection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;

public class AssignmentDtos {
    public record AssignmentRequest(
            @NotBlank(message = "Title is required") String title,
            String description,
            @NotNull(message = "Algorithm is required") AlgorithmType algorithm,
            @NotNull(message = "Direction is required") SortDirection direction,
            @NotBlank(message = "Input data is required") String inputData,
            LocalDateTime dueDate,
            AssignmentStatus status
    ) {}

    public record AssignmentResponse(
            Long id,
            String title,
            String description,
            AlgorithmType algorithm,
            SortDirection direction,
            String inputData,
            LocalDateTime dueDate,
            Long classId,
            String teacherId,
            AssignmentStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
