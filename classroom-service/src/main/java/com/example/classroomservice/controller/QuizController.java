package com.example.classroomservice.controller;

import com.example.classroomservice.dto.QuizDtos.QuizResultRequest;
import com.example.classroomservice.dto.QuizDtos.QuizResultResponse;
import com.example.classroomservice.security.AuthenticatedUser;
import com.example.classroomservice.service.ClassroomService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class QuizController {
    private final ClassroomService classroomService;

    public QuizController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @PostMapping("/api/assignments/{assignmentId}/quiz-results")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('STUDENT')")
    public QuizResultResponse saveQuizResult(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long assignmentId,
            @RequestBody QuizResultRequest request
    ) {
        return classroomService.saveQuizResult(user.getEmail(), assignmentId, request);
    }

    @GetMapping("/api/assignments/{assignmentId}/quiz-results")
    @PreAuthorize("hasRole('PROFESOR')")
    public List<QuizResultResponse> assignmentQuizResults(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long assignmentId) {
        return classroomService.assignmentQuizResults(user.getEmail(), assignmentId);
    }

    @GetMapping("/api/students/me/quiz-results")
    @PreAuthorize("hasRole('STUDENT')")
    public List<QuizResultResponse> myQuizResults(@AuthenticationPrincipal AuthenticatedUser user) {
        return classroomService.myQuizResults(user.getEmail());
    }
}
