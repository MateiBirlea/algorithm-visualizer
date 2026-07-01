package com.example.classroomservice.dto;

import jakarta.validation.Valid;

import java.time.Instant;
import java.util.List;

public class QuizDtos {
    public record QuizAnswerRequest(
            String question,
            String selectedAnswer,
            String correctAnswer,
            Boolean correct,
            String explanation
    ) {}

    public record QuizResultRequest(
            Integer score,
            Integer totalQuestions,
            Integer correctAnswers,
            Integer wrongAnswers,
            @Valid List<QuizAnswerRequest> answers
    ) {}

    public record QuizAnswerResponse(
            Long id,
            String question,
            String selectedAnswer,
            String correctAnswer,
            boolean correct,
            String explanation
    ) {}

    public record QuizResultResponse(
            Long id,
            String studentId,
            Long assignmentId,
            Integer score,
            Integer totalQuestions,
            Integer correctAnswers,
            Integer wrongAnswers,
            Instant createdAt,
            List<QuizAnswerResponse> answers
    ) {}
}
