package com.example.classroomservice.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public class ClassroomDtos {
    public record ClassroomRequest(
            @NotBlank(message = "Name is required") String name,
            String description
    ) {}

    public record JoinClassRequest(
            @NotBlank(message = "Join code is required") String joinCode
    ) {}

    public record ClassroomResponse(
            Long id,
            String name,
            String description,
            String joinCode,
            String teacherId,
            long studentCount,
            long assignmentCount,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record EnrollmentResponse(
            Long id,
            String studentId,
            Instant enrolledAt
    ) {}
}
