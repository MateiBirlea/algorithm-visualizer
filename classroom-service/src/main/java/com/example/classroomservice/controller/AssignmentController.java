package com.example.classroomservice.controller;

import com.example.classroomservice.dto.AssignmentDtos.AssignmentRequest;
import com.example.classroomservice.dto.AssignmentDtos.AssignmentResponse;
import com.example.classroomservice.dto.ProgressDtos.ProgressResponse;
import com.example.classroomservice.dto.ProgressDtos.SubmitProgressRequest;
import com.example.classroomservice.security.AuthenticatedUser;
import com.example.classroomservice.service.ClassroomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AssignmentController {
    private final ClassroomService classroomService;

    public AssignmentController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @PostMapping("/api/classes/{classId}/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PROFESOR')")
    public AssignmentResponse createAssignment(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long classId,
            @Valid @RequestBody AssignmentRequest request
    ) {
        return classroomService.createAssignment(user.getEmail(), classId, request);
    }

    @GetMapping("/api/classes/{classId}/assignments")
    public List<AssignmentResponse> classAssignments(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long classId) {
        return classroomService.classAssignments(user.getEmail(), classId);
    }

    @GetMapping("/api/assignments/{assignmentId}")
    public AssignmentResponse getAssignment(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long assignmentId) {
        return classroomService.getAssignment(user.getEmail(), assignmentId);
    }

    @PutMapping("/api/assignments/{assignmentId}")
    @PreAuthorize("hasRole('PROFESOR')")
    public AssignmentResponse updateAssignment(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long assignmentId,
            @Valid @RequestBody AssignmentRequest request
    ) {
        return classroomService.updateAssignment(user.getEmail(), assignmentId, request);
    }

    @DeleteMapping("/api/assignments/{assignmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('PROFESOR')")
    public void deleteAssignment(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long assignmentId) {
        classroomService.deleteAssignment(user.getEmail(), assignmentId);
    }

    @PostMapping("/api/assignments/{assignmentId}/publish")
    @PreAuthorize("hasRole('PROFESOR')")
    public AssignmentResponse publishAssignment(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long assignmentId) {
        return classroomService.publishAssignment(user.getEmail(), assignmentId);
    }

    @PostMapping("/api/assignments/{assignmentId}/start")
    @PreAuthorize("hasRole('STUDENT')")
    public ProgressResponse startAssignment(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long assignmentId) {
        return classroomService.startAssignment(user.getEmail(), assignmentId);
    }

    @PostMapping("/api/assignments/{assignmentId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ProgressResponse submitAssignment(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long assignmentId,
            @RequestBody SubmitProgressRequest request
    ) {
        return classroomService.submitAssignment(user.getEmail(), assignmentId, request);
    }

    @GetMapping("/api/assignments/{assignmentId}/progress")
    @PreAuthorize("hasRole('PROFESOR')")
    public List<ProgressResponse> assignmentProgress(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long assignmentId) {
        return classroomService.assignmentProgress(user.getEmail(), assignmentId);
    }

    @GetMapping("/api/students/me/progress")
    @PreAuthorize("hasRole('STUDENT')")
    public List<ProgressResponse> myProgress(@AuthenticationPrincipal AuthenticatedUser user) {
        return classroomService.myProgress(user.getEmail());
    }

    @GetMapping("/api/classes/{classId}/progress")
    @PreAuthorize("hasRole('PROFESOR')")
    public List<ProgressResponse> classProgress(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long classId) {
        return classroomService.classProgress(user.getEmail(), classId);
    }
}
