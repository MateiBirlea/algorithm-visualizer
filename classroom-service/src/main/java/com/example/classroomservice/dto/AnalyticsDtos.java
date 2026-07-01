package com.example.classroomservice.dto;

import java.util.List;

public class AnalyticsDtos {
    public record TeacherDashboardResponse(
            long totalClasses,
            long totalStudents,
            long totalAssignments,
            long publishedAssignments,
            double completionRate,
            double averageQuizScore,
            String mostUsedAlgorithm,
            List<String> topStudents,
            List<String> studentsNeedingAttention
    ) {}

    public record ClassStatsResponse(
            Long classId,
            long studentCount,
            long assignmentCount,
            double completionRate,
            double averageQuizScore,
            double averageRunCount,
            double averageExecutionTimeMs,
            long totalRuns,
            String mostUsedAlgorithm,
            List<String> resultDistribution
    ) {}

    public record AiAnalysisResponse(
            Long classId,
            String understoodConcepts,
            String difficultConcepts,
            String frequentMistakes,
            String teacherRecommendations,
            List<String> studentsNeedingSupport,
            String conclusion,
            String fullAnalysis
    ) {}
}
