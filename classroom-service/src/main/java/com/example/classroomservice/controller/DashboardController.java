package com.example.classroomservice.controller;

import com.example.classroomservice.dto.AnalyticsDtos.AiAnalysisResponse;
import com.example.classroomservice.dto.AnalyticsDtos.ClassStatsResponse;
import com.example.classroomservice.dto.AnalyticsDtos.TeacherDashboardResponse;
import com.example.classroomservice.security.AuthenticatedUser;
import com.example.classroomservice.service.ClassroomService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController {
    private final ClassroomService classroomService;

    public DashboardController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @GetMapping("/api/teacher/dashboard")
    @PreAuthorize("hasRole('PROFESOR')")
    public TeacherDashboardResponse teacherDashboard(@AuthenticationPrincipal AuthenticatedUser user) {
        return classroomService.teacherDashboard(user.getEmail());
    }

    @GetMapping("/api/classes/{classId}/stats")
    @PreAuthorize("hasRole('PROFESOR')")
    public ClassStatsResponse classStats(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long classId) {
        return classroomService.classStats(user.getEmail(), classId);
    }

    @GetMapping("/api/classes/{classId}/ai-analysis")
    @PreAuthorize("hasRole('PROFESOR')")
    public AiAnalysisResponse classAiAnalysis(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long classId) {
        return classroomService.classAiAnalysis(user.getEmail(), classId);
    }

    @GetMapping("/api/classes/{classId}/report/pdf")
    @PreAuthorize("hasRole('PROFESOR')")
    public ResponseEntity<byte[]> classReport(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long classId) {
        byte[] report = classroomService.classReport(user.getEmail(), classId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("class-report-" + classId + ".pdf").build().toString())
                .body(report);
    }
}
