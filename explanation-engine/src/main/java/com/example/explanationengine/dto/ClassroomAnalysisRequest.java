package com.example.explanationengine.dto;

import java.util.List;

public class ClassroomAnalysisRequest {
    private Long classId;
    private String className;
    private long studentCount;
    private long assignmentCount;
    private long completedAssignments;
    private long notStartedAssignments;
    private long inProgressAssignments;
    private double completionRate;
    private double averageQuizScore;
    private double averageRunCount;
    private long totalRuns;
    private String mostUsedAlgorithm;
    private List<String> algorithmsUsed;
    private List<String> resultDistribution;
    private List<String> studentsNeedingAttention;
    private List<StudentStats> studentStats;
    private List<AssignmentStats> assignmentStats;

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public long getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(long studentCount) {
        this.studentCount = studentCount;
    }

    public long getAssignmentCount() {
        return assignmentCount;
    }

    public void setAssignmentCount(long assignmentCount) {
        this.assignmentCount = assignmentCount;
    }

    public long getCompletedAssignments() {
        return completedAssignments;
    }

    public void setCompletedAssignments(long completedAssignments) {
        this.completedAssignments = completedAssignments;
    }

    public long getNotStartedAssignments() {
        return notStartedAssignments;
    }

    public void setNotStartedAssignments(long notStartedAssignments) {
        this.notStartedAssignments = notStartedAssignments;
    }

    public long getInProgressAssignments() {
        return inProgressAssignments;
    }

    public void setInProgressAssignments(long inProgressAssignments) {
        this.inProgressAssignments = inProgressAssignments;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(double completionRate) {
        this.completionRate = completionRate;
    }

    public double getAverageQuizScore() {
        return averageQuizScore;
    }

    public void setAverageQuizScore(double averageQuizScore) {
        this.averageQuizScore = averageQuizScore;
    }

    public double getAverageRunCount() {
        return averageRunCount;
    }

    public void setAverageRunCount(double averageRunCount) {
        this.averageRunCount = averageRunCount;
    }

    public long getTotalRuns() {
        return totalRuns;
    }

    public void setTotalRuns(long totalRuns) {
        this.totalRuns = totalRuns;
    }

    public String getMostUsedAlgorithm() {
        return mostUsedAlgorithm;
    }

    public void setMostUsedAlgorithm(String mostUsedAlgorithm) {
        this.mostUsedAlgorithm = mostUsedAlgorithm;
    }

    public List<String> getAlgorithmsUsed() {
        return algorithmsUsed;
    }

    public void setAlgorithmsUsed(List<String> algorithmsUsed) {
        this.algorithmsUsed = algorithmsUsed;
    }

    public List<String> getResultDistribution() {
        return resultDistribution;
    }

    public void setResultDistribution(List<String> resultDistribution) {
        this.resultDistribution = resultDistribution;
    }

    public List<String> getStudentsNeedingAttention() {
        return studentsNeedingAttention;
    }

    public void setStudentsNeedingAttention(List<String> studentsNeedingAttention) {
        this.studentsNeedingAttention = studentsNeedingAttention;
    }

    public List<StudentStats> getStudentStats() {
        return studentStats;
    }

    public void setStudentStats(List<StudentStats> studentStats) {
        this.studentStats = studentStats;
    }

    public List<AssignmentStats> getAssignmentStats() {
        return assignmentStats;
    }

    public void setAssignmentStats(List<AssignmentStats> assignmentStats) {
        this.assignmentStats = assignmentStats;
    }

    public static class StudentStats {
        private String studentId;
        private long completedAssignments;
        private long inProgressAssignments;
        private long notStartedAssignments;
        private long totalRuns;
        private long totalComparisons;
        private long totalSwaps;
        private long totalSteps;
        private double averageQuizScore;

        public String getStudentId() {
            return studentId;
        }

        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }

        public long getCompletedAssignments() {
            return completedAssignments;
        }

        public void setCompletedAssignments(long completedAssignments) {
            this.completedAssignments = completedAssignments;
        }

        public long getInProgressAssignments() {
            return inProgressAssignments;
        }

        public void setInProgressAssignments(long inProgressAssignments) {
            this.inProgressAssignments = inProgressAssignments;
        }

        public long getNotStartedAssignments() {
            return notStartedAssignments;
        }

        public void setNotStartedAssignments(long notStartedAssignments) {
            this.notStartedAssignments = notStartedAssignments;
        }

        public long getTotalRuns() {
            return totalRuns;
        }

        public void setTotalRuns(long totalRuns) {
            this.totalRuns = totalRuns;
        }

        public long getTotalComparisons() {
            return totalComparisons;
        }

        public void setTotalComparisons(long totalComparisons) {
            this.totalComparisons = totalComparisons;
        }

        public long getTotalSwaps() {
            return totalSwaps;
        }

        public void setTotalSwaps(long totalSwaps) {
            this.totalSwaps = totalSwaps;
        }

        public long getTotalSteps() {
            return totalSteps;
        }

        public void setTotalSteps(long totalSteps) {
            this.totalSteps = totalSteps;
        }

        public double getAverageQuizScore() {
            return averageQuizScore;
        }

        public void setAverageQuizScore(double averageQuizScore) {
            this.averageQuizScore = averageQuizScore;
        }
    }

    public static class AssignmentStats {
        private Long assignmentId;
        private String title;
        private String algorithm;
        private String direction;
        private String status;
        private long completedCount;
        private long inProgressCount;
        private long notStartedCount;
        private long totalRuns;
        private double averageQuizScore;

        public Long getAssignmentId() {
            return assignmentId;
        }

        public void setAssignmentId(Long assignmentId) {
            this.assignmentId = assignmentId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public long getCompletedCount() {
            return completedCount;
        }

        public void setCompletedCount(long completedCount) {
            this.completedCount = completedCount;
        }

        public long getInProgressCount() {
            return inProgressCount;
        }

        public void setInProgressCount(long inProgressCount) {
            this.inProgressCount = inProgressCount;
        }

        public long getNotStartedCount() {
            return notStartedCount;
        }

        public void setNotStartedCount(long notStartedCount) {
            this.notStartedCount = notStartedCount;
        }

        public long getTotalRuns() {
            return totalRuns;
        }

        public void setTotalRuns(long totalRuns) {
            this.totalRuns = totalRuns;
        }

        public double getAverageQuizScore() {
            return averageQuizScore;
        }

        public void setAverageQuizScore(double averageQuizScore) {
            this.averageQuizScore = averageQuizScore;
        }
    }
}
