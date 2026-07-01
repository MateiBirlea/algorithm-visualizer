package com.example.classroomservice.service;

import com.example.classroomservice.dto.AnalyticsDtos.AiAnalysisResponse;
import com.example.classroomservice.dto.AnalyticsDtos.ClassStatsResponse;
import com.example.classroomservice.dto.AnalyticsDtos.TeacherDashboardResponse;
import com.example.classroomservice.dto.AssignmentDtos.AssignmentRequest;
import com.example.classroomservice.dto.AssignmentDtos.AssignmentResponse;
import com.example.classroomservice.dto.ClassroomDtos.ClassroomRequest;
import com.example.classroomservice.dto.ClassroomDtos.ClassroomResponse;
import com.example.classroomservice.dto.ClassroomDtos.EnrollmentResponse;
import com.example.classroomservice.dto.ProgressDtos.ProgressResponse;
import com.example.classroomservice.dto.ProgressDtos.SubmitProgressRequest;
import com.example.classroomservice.dto.QuizDtos.QuizAnswerResponse;
import com.example.classroomservice.dto.QuizDtos.QuizResultRequest;
import com.example.classroomservice.dto.QuizDtos.QuizResultResponse;
import com.example.classroomservice.model.AlgorithmType;
import com.example.classroomservice.model.Assignment;
import com.example.classroomservice.model.AssignmentStatus;
import com.example.classroomservice.model.ClassEnrollment;
import com.example.classroomservice.model.Classroom;
import com.example.classroomservice.model.ProgressStatus;
import com.example.classroomservice.model.QuizAnswer;
import com.example.classroomservice.model.QuizResult;
import com.example.classroomservice.model.StudentProgress;
import com.example.classroomservice.repository.AssignmentRepository;
import com.example.classroomservice.repository.ClassEnrollmentRepository;
import com.example.classroomservice.repository.ClassroomRepository;
import com.example.classroomservice.repository.QuizResultRepository;
import com.example.classroomservice.repository.StudentProgressRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class ClassroomService {
    private final ClassroomRepository classroomRepository;
    private final ClassEnrollmentRepository enrollmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final StudentProgressRepository progressRepository;
    private final QuizResultRepository quizResultRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String explanationEngineUrl;

    public ClassroomService(
            ClassroomRepository classroomRepository,
            ClassEnrollmentRepository enrollmentRepository,
            AssignmentRepository assignmentRepository,
            StudentProgressRepository progressRepository,
            QuizResultRepository quizResultRepository,
            @Value("${explanation-engine.base-url}") String explanationEngineUrl
    ) {
        this.classroomRepository = classroomRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.assignmentRepository = assignmentRepository;
        this.progressRepository = progressRepository;
        this.quizResultRepository = quizResultRepository;
        this.explanationEngineUrl = explanationEngineUrl;
    }

    public ClassroomResponse createClassroom(String teacherId, ClassroomRequest request) {
        Classroom classroom = new Classroom();
        classroom.setName(request.name());
        classroom.setDescription(request.description());
        classroom.setTeacherId(teacherId);
        classroom.setJoinCode(generateJoinCode());
        return toClassroomResponse(classroomRepository.save(classroom));
    }

    @Transactional(readOnly = true)
    public List<ClassroomResponse> myTeacherClasses(String teacherId) {
        return classroomRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId).stream()
                .map(this::toClassroomResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClassroomResponse> myStudentClasses(String studentId) {
        return enrollmentRepository.findByStudentIdOrderByEnrolledAtDesc(studentId).stream()
                .map(enrollment -> toClassroomResponse(enrollment.getClassroom()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ClassroomResponse getClassroom(String userId, Long classId) {
        Classroom classroom = getClassroomOrThrow(classId);
        requireTeacherOrEnrolledStudent(classroom, userId);
        return toClassroomResponse(classroom);
    }

    public ClassroomResponse updateClassroom(String teacherId, Long classId, ClassroomRequest request) {
        Classroom classroom = getOwnedClassroom(teacherId, classId);
        classroom.setName(request.name());
        classroom.setDescription(request.description());
        return toClassroomResponse(classroom);
    }

    public void deleteClassroom(String teacherId, Long classId) {
        Classroom classroom = getOwnedClassroom(teacherId, classId);
        List<Assignment> assignments = assignmentRepository.findByClassroomOrderByCreatedAtDesc(classroom);
        if (!assignments.isEmpty()) {
            quizResultRepository.deleteByAssignmentIn(assignments);
            progressRepository.deleteByAssignmentIn(assignments);
            assignmentRepository.deleteAll(assignments);
        }
        enrollmentRepository.deleteByClassroom(classroom);
        classroomRepository.delete(classroom);
    }

    public ClassroomResponse joinClass(String studentId, String joinCode) {
        Classroom classroom = classroomRepository.findByJoinCode(joinCode.trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));
        if (!enrollmentRepository.existsByClassroomAndStudentId(classroom, studentId)) {
            ClassEnrollment enrollment = new ClassEnrollment();
            enrollment.setClassroom(classroom);
            enrollment.setStudentId(studentId);
            enrollmentRepository.save(enrollment);
        }
        return toClassroomResponse(classroom);
    }

    public void leaveClass(String studentId, Long classId) {
        Classroom classroom = getClassroomOrThrow(classId);
        ClassEnrollment enrollment = enrollmentRepository.findByClassroomAndStudentId(classroom, studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
        enrollmentRepository.delete(enrollment);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> classStudents(String teacherId, Long classId) {
        Classroom classroom = getOwnedClassroom(teacherId, classId);
        return enrollmentRepository.findByClassroomOrderByEnrolledAtAsc(classroom).stream()
                .map(e -> new EnrollmentResponse(e.getId(), e.getStudentId(), e.getEnrolledAt()))
                .toList();
    }

    public AssignmentResponse createAssignment(String teacherId, Long classId, AssignmentRequest request) {
        Classroom classroom = getOwnedClassroom(teacherId, classId);
        validateAssignment(request);
        Assignment assignment = new Assignment();
        applyAssignmentRequest(assignment, request);
        assignment.setClassroom(classroom);
        assignment.setTeacherId(teacherId);
        return toAssignmentResponse(assignmentRepository.save(assignment));
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> classAssignments(String userId, Long classId) {
        Classroom classroom = getClassroomOrThrow(classId);
        if (classroom.getTeacherId().equals(userId)) {
            return assignmentRepository.findByClassroomOrderByCreatedAtDesc(classroom).stream().map(this::toAssignmentResponse).toList();
        }
        requireEnrolled(classroom, userId);
        return assignmentRepository.findByClassroomAndStatusOrderByCreatedAtDesc(classroom, AssignmentStatus.PUBLISHED).stream()
                .map(this::toAssignmentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AssignmentResponse getAssignment(String userId, Long assignmentId) {
        Assignment assignment = getAssignmentOrThrow(assignmentId);
        requireAssignmentAccess(userId, assignment);
        return toAssignmentResponse(assignment);
    }

    public AssignmentResponse updateAssignment(String teacherId, Long assignmentId, AssignmentRequest request) {
        Assignment assignment = getAssignmentOrThrow(assignmentId);
        requireTeacher(assignment, teacherId);
        validateAssignment(request);
        applyAssignmentRequest(assignment, request);
        return toAssignmentResponse(assignment);
    }

    public void deleteAssignment(String teacherId, Long assignmentId) {
        Assignment assignment = getAssignmentOrThrow(assignmentId);
        requireTeacher(assignment, teacherId);
        quizResultRepository.deleteByAssignment(assignment);
        progressRepository.deleteByAssignment(assignment);
        assignmentRepository.delete(assignment);
    }

    public AssignmentResponse publishAssignment(String teacherId, Long assignmentId) {
        Assignment assignment = getAssignmentOrThrow(assignmentId);
        requireTeacher(assignment, teacherId);
        assignment.setStatus(AssignmentStatus.PUBLISHED);
        return toAssignmentResponse(assignment);
    }

    public ProgressResponse startAssignment(String studentId, Long assignmentId) {
        Assignment assignment = getAssignmentOrThrow(assignmentId);
        requirePublishedAssignmentForStudent(studentId, assignment);
        StudentProgress progress = getOrCreateProgress(studentId, assignment);
        if (progress.getStatus() == ProgressStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Assignment is already completed");
        }
        rejectIfOverdue(assignment);
        if (progress.getStartedAt() == null) {
            progress.setStartedAt(Instant.now());
        }
        progress.setStatus(ProgressStatus.IN_PROGRESS);
        return toProgressResponse(progress);
    }

    public ProgressResponse submitAssignment(String studentId, Long assignmentId, SubmitProgressRequest request) {
        Assignment assignment = getAssignmentOrThrow(assignmentId);
        requirePublishedAssignmentForStudent(studentId, assignment);
        StudentProgress progress = getOrCreateProgress(studentId, assignment);
        if (progress.getStatus() == ProgressStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Assignment is already completed");
        }
        rejectIfOverdue(assignment);
        if (progress.getStartedAt() == null) {
            progress.setStartedAt(Instant.now());
        }
        progress.setStatus(ProgressStatus.COMPLETED);
        progress.setCompletedAt(Instant.now());
        progress.setLastRunAt(Instant.now());
        progress.setRunCount((progress.getRunCount() == null ? 0 : progress.getRunCount()) + 1);
        progress.setAlgorithm(request.algorithm() == null ? assignment.getAlgorithm() : request.algorithm());
        progress.setDirection(request.direction() == null ? assignment.getDirection() : request.direction());
        progress.setInputData(request.inputData() == null ? assignment.getInputData() : request.inputData());
        progress.setSortedOutput(request.sortedOutput());
        progress.setTotalSteps(valueOrZero(request.totalSteps()));
        progress.setTotalComparisons(valueOrZero(request.totalComparisons()));
        progress.setTotalSwaps(valueOrZero(request.totalSwaps()));
        progress.setExecutionTimeMs(request.executionTimeMs() == null ? 0L : request.executionTimeMs());
        return toProgressResponse(progress);
    }

    @Transactional(readOnly = true)
    public List<ProgressResponse> assignmentProgress(String teacherId, Long assignmentId) {
        Assignment assignment = getAssignmentOrThrow(assignmentId);
        requireTeacher(assignment, teacherId);
        return progressRepository.findByAssignmentOrderByStudentIdAsc(assignment).stream().map(this::toProgressResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProgressResponse> myProgress(String studentId) {
        return progressRepository.findByStudentIdOrderByStartedAtDesc(studentId).stream().map(this::toProgressResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProgressResponse> classProgress(String teacherId, Long classId) {
        Classroom classroom = getOwnedClassroom(teacherId, classId);
        List<Assignment> assignments = assignmentRepository.findByClassroomOrderByCreatedAtDesc(classroom);
        List<ClassEnrollment> enrollments = enrollmentRepository.findByClassroomOrderByEnrolledAtAsc(classroom);
        List<StudentProgress> savedProgress = assignments.isEmpty() ? List.of() : progressRepository.findByAssignmentIn(assignments);
        Map<String, StudentProgress> progressByAssignmentAndStudent = new HashMap<>();
        savedProgress.forEach(progress -> progressByAssignmentAndStudent.put(progressKey(progress.getAssignment().getId(), progress.getStudentId()), progress));

        List<ProgressResponse> result = new ArrayList<>();
        for (Assignment assignment : assignments) {
            for (ClassEnrollment enrollment : enrollments) {
                StudentProgress progress = progressByAssignmentAndStudent.get(progressKey(assignment.getId(), enrollment.getStudentId()));
                result.add(progress == null ? toMissingProgressResponse(assignment, enrollment.getStudentId()) : toProgressResponse(progress));
            }
        }
        return result;
    }

    public QuizResultResponse saveQuizResult(String studentId, Long assignmentId, QuizResultRequest request) {
        Assignment assignment = getAssignmentOrThrow(assignmentId);
        requirePublishedAssignmentForStudent(studentId, assignment);
        QuizResult result = new QuizResult();
        result.setStudentId(studentId);
        result.setAssignment(assignment);
        result.setScore(valueOrZero(request.score()));
        result.setTotalQuestions(valueOrZero(request.totalQuestions()));
        result.setCorrectAnswers(valueOrZero(request.correctAnswers()));
        result.setWrongAnswers(valueOrZero(request.wrongAnswers()));
        if (request.answers() != null) {
            request.answers().forEach(answerRequest -> {
                QuizAnswer answer = new QuizAnswer();
                answer.setQuizResult(result);
                answer.setQuestion(answerRequest.question());
                answer.setSelectedAnswer(answerRequest.selectedAnswer());
                answer.setCorrectAnswer(answerRequest.correctAnswer());
                answer.setCorrect(Boolean.TRUE.equals(answerRequest.correct()));
                answer.setExplanation(answerRequest.explanation());
                result.getAnswers().add(answer);
            });
        }
        return toQuizResultResponse(quizResultRepository.save(result));
    }

    @Transactional(readOnly = true)
    public List<QuizResultResponse> assignmentQuizResults(String teacherId, Long assignmentId) {
        Assignment assignment = getAssignmentOrThrow(assignmentId);
        requireTeacher(assignment, teacherId);
        return quizResultRepository.findByAssignmentOrderByCreatedAtDesc(assignment).stream().map(this::toQuizResultResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<QuizResultResponse> myQuizResults(String studentId) {
        return quizResultRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream().map(this::toQuizResultResponse).toList();
    }

    @Transactional(readOnly = true)
    public TeacherDashboardResponse teacherDashboard(String teacherId) {
        List<Classroom> classes = classroomRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);
        List<Assignment> assignments = assignmentRepository.findByTeacherId(teacherId);
        List<StudentProgress> progress = assignments.isEmpty() ? List.of() : progressRepository.findByAssignmentIn(assignments);
        List<QuizResult> quizResults = assignments.isEmpty() ? List.of() : quizResultRepository.findByAssignmentIn(assignments);
        long totalStudents = classes.stream().mapToLong(enrollmentRepository::countByClassroom).sum();
        return new TeacherDashboardResponse(
                classes.size(),
                totalStudents,
                assignments.size(),
                assignmentRepository.countByTeacherIdAndStatus(teacherId, AssignmentStatus.PUBLISHED),
                completionRate(progress, assignments.size(), Math.max(totalStudents, 1)),
                averageQuizScore(quizResults),
                mostUsedAlgorithm(progress),
                topStudents(quizResults),
                studentsNeedingAttention(progress, classes)
        );
    }

    @Transactional(readOnly = true)
    public ClassStatsResponse classStats(String teacherId, Long classId) {
        Classroom classroom = getOwnedClassroom(teacherId, classId);
        List<Assignment> assignments = assignmentRepository.findByClassroomOrderByCreatedAtDesc(classroom);
        List<StudentProgress> progress = assignments.isEmpty() ? List.of() : progressRepository.findByAssignmentIn(assignments);
        List<QuizResult> quizzes = assignments.isEmpty() ? List.of() : quizResultRepository.findByAssignmentIn(assignments);
        long students = enrollmentRepository.countByClassroom(classroom);
        long totalRuns = progress.stream().mapToLong(p -> p.getRunCount() == null ? 0 : p.getRunCount()).sum();
        double avgRuns = progress.isEmpty() ? 0 : (double) totalRuns / progress.size();
        double avgTime = progress.stream().mapToLong(p -> p.getExecutionTimeMs() == null ? 0 : p.getExecutionTimeMs()).average().orElse(0);
        return new ClassStatsResponse(
                classroom.getId(),
                students,
                assignments.size(),
                completionRate(progress, assignments.size(), Math.max(students, 1)),
                averageQuizScore(quizzes),
                avgRuns,
                avgTime,
                totalRuns,
                mostUsedAlgorithm(progress),
                resultDistribution(progress)
        );
    }

    @Transactional(readOnly = true)
    public AiAnalysisResponse classAiAnalysis(String teacherId, Long classId) {
        Classroom classroom = getOwnedClassroom(teacherId, classId);
        ClassStatsResponse stats = classStats(teacherId, classId);
        ClassroomAiData aiData = buildClassroomAiData(classroom, stats);
        List<String> attention = aiData.studentsNeedingAttention();
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("classId", classId);
            request.put("className", classroom.getName());
            request.put("studentCount", stats.studentCount());
            request.put("assignmentCount", stats.assignmentCount());
            request.put("completedAssignments", aiData.completedAssignments());
            request.put("notStartedAssignments", aiData.notStartedAssignments());
            request.put("inProgressAssignments", aiData.inProgressAssignments());
            request.put("completionRate", stats.completionRate());
            request.put("averageQuizScore", stats.averageQuizScore());
            request.put("averageRunCount", stats.averageRunCount());
            request.put("totalRuns", stats.totalRuns());
            request.put("mostUsedAlgorithm", stats.mostUsedAlgorithm());
            request.put("algorithmsUsed", aiData.algorithmsUsed());
            request.put("resultDistribution", stats.resultDistribution());
            request.put("studentsNeedingAttention", attention);
            request.put("studentStats", aiData.studentStats());
            request.put("assignmentStats", aiData.assignmentStats());
            Map<?, ?> response = restTemplate.postForObject(explanationEngineUrl + "/api/explanations/classroom-analysis", request, Map.class);
            Object explanationValue = response == null ? null : response.get("explanation");
            String explanation = explanationValue == null ? "" : String.valueOf(explanationValue);
            return aiAnalysisResponse(classId, explanation, attention, stats, aiData);
        } catch (RuntimeException ex) {
            return fallbackAiAnalysis(classId, attention, stats, aiData);
        }
    }

    @Transactional(readOnly = true)
    public byte[] classReport(String teacherId, Long classId) {
        Classroom classroom = getOwnedClassroom(teacherId, classId);
        ClassStatsResponse stats = classStats(teacherId, classId);
        AiAnalysisResponse analysis = classAiAnalysis(teacherId, classId);
        String text = "Raport clasa: " + classroom.getName() + "\n"
                + "Profesor: " + teacherId + "\n"
                + "Elevi: " + stats.studentCount() + "\n"
                + "Teme: " + stats.assignmentCount() + "\n"
                + "Rata finalizare: " + stats.completionRate() + "%\n"
                + "Scor mediu quiz: " + stats.averageQuizScore() + "\n"
                + "Analiza AI: " + analysis.conclusion() + "\n";
        return minimalPdf(text);
    }

    private Classroom getClassroomOrThrow(Long classId) {
        return classroomRepository.findById(classId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));
    }

    private Classroom getOwnedClassroom(String teacherId, Long classId) {
        Classroom classroom = getClassroomOrThrow(classId);
        if (!classroom.getTeacherId().equals(teacherId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Class belongs to another teacher");
        }
        return classroom;
    }

    private Assignment getAssignmentOrThrow(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));
    }

    private void requireTeacher(Assignment assignment, String teacherId) {
        if (!assignment.getTeacherId().equals(teacherId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Assignment belongs to another teacher");
        }
    }

    private void requireEnrolled(Classroom classroom, String studentId) {
        if (!enrollmentRepository.existsByClassroomAndStudentId(classroom, studentId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student is not enrolled in this class");
        }
    }

    private void requireTeacherOrEnrolledStudent(Classroom classroom, String userId) {
        if (!classroom.getTeacherId().equals(userId)) {
            requireEnrolled(classroom, userId);
        }
    }

    private void requireAssignmentAccess(String userId, Assignment assignment) {
        if (assignment.getTeacherId().equals(userId)) {
            return;
        }
        if (assignment.getStatus() != AssignmentStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Assignment is not published");
        }
        requireEnrolled(assignment.getClassroom(), userId);
    }

    private void requirePublishedAssignmentForStudent(String studentId, Assignment assignment) {
        if (assignment.getStatus() != AssignmentStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Assignment is not published");
        }
        requireEnrolled(assignment.getClassroom(), studentId);
    }

    private StudentProgress getOrCreateProgress(String studentId, Assignment assignment) {
        return progressRepository.findByAssignmentAndStudentId(assignment, studentId).orElseGet(() -> {
            StudentProgress progress = new StudentProgress();
            progress.setAssignment(assignment);
            progress.setStudentId(studentId);
            progress.setAlgorithm(assignment.getAlgorithm());
            progress.setDirection(assignment.getDirection());
            progress.setInputData(assignment.getInputData());
            return progressRepository.save(progress);
        });
    }

    private void validateAssignment(AssignmentRequest request) {
        if (request.dueDate() != null && !request.dueDate().toLocalDate().isAfter(LocalDateTime.now().toLocalDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Due date must be after today");
        }
        int valueCount = request.inputData().split("[,;\\s]+").length;
        if (valueCount < 2 || valueCount > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Input data must contain between 2 and 100 values");
        }
    }

    private void applyAssignmentRequest(Assignment assignment, AssignmentRequest request) {
        assignment.setTitle(request.title());
        assignment.setDescription(request.description());
        assignment.setAlgorithm(request.algorithm());
        assignment.setDirection(request.direction());
        assignment.setInputData(request.inputData());
        assignment.setDueDate(request.dueDate());
        assignment.setStatus(request.status() == null ? AssignmentStatus.DRAFT : request.status());
    }

    private ClassroomResponse toClassroomResponse(Classroom classroom) {
        return new ClassroomResponse(
                classroom.getId(),
                classroom.getName(),
                classroom.getDescription(),
                classroom.getJoinCode(),
                classroom.getTeacherId(),
                enrollmentRepository.countByClassroom(classroom),
                assignmentRepository.countByClassroom(classroom),
                classroom.getCreatedAt(),
                classroom.getUpdatedAt()
        );
    }

    private AssignmentResponse toAssignmentResponse(Assignment assignment) {
        return new AssignmentResponse(
                assignment.getId(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getAlgorithm(),
                assignment.getDirection(),
                assignment.getInputData(),
                assignment.getDueDate(),
                assignment.getClassroom().getId(),
                assignment.getTeacherId(),
                assignment.getStatus(),
                assignment.getCreatedAt(),
                assignment.getUpdatedAt()
        );
    }

    private ProgressResponse toProgressResponse(StudentProgress progress) {
        return new ProgressResponse(
                progress.getId(),
                progress.getStudentId(),
                progress.getAssignment().getId(),
                progress.getStatus(),
                progress.getStartedAt(),
                progress.getCompletedAt(),
                progress.getRunCount(),
                progress.getLastRunAt(),
                progress.getAlgorithm(),
                progress.getDirection(),
                progress.getInputData(),
                progress.getSortedOutput(),
                progress.getTotalSteps(),
                progress.getTotalComparisons(),
                progress.getTotalSwaps(),
                progress.getExecutionTimeMs()
        );
    }

    private ProgressResponse toMissingProgressResponse(Assignment assignment, String studentId) {
        return new ProgressResponse(
                null,
                studentId,
                assignment.getId(),
                ProgressStatus.NOT_STARTED,
                null,
                null,
                0,
                null,
                assignment.getAlgorithm(),
                assignment.getDirection(),
                assignment.getInputData(),
                null,
                0,
                0,
                0,
                0L
        );
    }

    private String progressKey(Long assignmentId, String studentId) {
        return assignmentId + "::" + studentId;
    }

    private void rejectIfOverdue(Assignment assignment) {
        if (assignment.getDueDate() != null && assignment.getDueDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Assignment deadline has passed");
        }
    }

    private QuizResultResponse toQuizResultResponse(QuizResult result) {
        return new QuizResultResponse(
                result.getId(),
                result.getStudentId(),
                result.getAssignment().getId(),
                result.getScore(),
                result.getTotalQuestions(),
                result.getCorrectAnswers(),
                result.getWrongAnswers(),
                result.getCreatedAt(),
                result.getAnswers().stream()
                        .map(a -> new QuizAnswerResponse(a.getId(), a.getQuestion(), a.getSelectedAnswer(), a.getCorrectAnswer(), a.isCorrect(), a.getExplanation()))
                        .toList()
        );
    }

    private String generateJoinCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (classroomRepository.existsByJoinCode(code));
        return code;
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private double averageQuizScore(List<QuizResult> quizResults) {
        return quizResults.stream().mapToInt(q -> q.getScore() == null ? 0 : q.getScore()).average().orElse(0);
    }

    private double completionRate(List<StudentProgress> progress, long assignmentCount, long studentCount) {
        if (assignmentCount == 0 || studentCount == 0) {
            return 0;
        }
        long completed = progress.stream().filter(p -> p.getStatus() == ProgressStatus.COMPLETED).count();
        return (completed * 100.0) / (assignmentCount * studentCount);
    }

    private String mostUsedAlgorithm(List<StudentProgress> progress) {
        Map<AlgorithmType, Long> counts = new HashMap<>();
        progress.stream().filter(p -> p.getAlgorithm() != null).forEach(p -> counts.merge(p.getAlgorithm(), 1L, Long::sum));
        return counts.entrySet().stream().max(Map.Entry.comparingByValue()).map(e -> e.getKey().name()).orElse("N/A");
    }

    private List<String> topStudents(List<QuizResult> quizResults) {
        return quizResults.stream()
                .sorted(Comparator.comparing((QuizResult q) -> q.getScore() == null ? 0 : q.getScore()).reversed())
                .limit(5)
                .map(q -> q.getStudentId() + " (" + q.getScore() + ")")
                .toList();
    }

    private List<String> studentsNeedingAttention(List<StudentProgress> progress, List<Classroom> classes) {
        List<String> students = new ArrayList<>();
        classes.forEach(c -> enrollmentRepository.findByClassroomOrderByEnrolledAtAsc(c).forEach(e -> students.add(e.getStudentId())));
        progress.stream().filter(p -> p.getStatus() == ProgressStatus.COMPLETED).map(StudentProgress::getStudentId).forEach(students::remove);
        return students.stream().distinct().limit(10).toList();
    }

    private List<String> resultDistribution(List<StudentProgress> progress) {
        long completed = progress.stream().filter(p -> p.getStatus() == ProgressStatus.COMPLETED).count();
        long inProgress = progress.stream().filter(p -> p.getStatus() == ProgressStatus.IN_PROGRESS).count();
        long notStarted = progress.stream().filter(p -> p.getStatus() == ProgressStatus.NOT_STARTED).count();
        return List.of("COMPLETED=" + completed, "IN_PROGRESS=" + inProgress, "NOT_STARTED=" + notStarted);
    }

    private ClassroomAiData buildClassroomAiData(Classroom classroom, ClassStatsResponse stats) {
        List<Assignment> assignments = assignmentRepository.findByClassroomOrderByCreatedAtDesc(classroom);
        List<ClassEnrollment> enrollments = enrollmentRepository.findByClassroomOrderByEnrolledAtAsc(classroom);
        List<StudentProgress> savedProgress = assignments.isEmpty() ? List.of() : progressRepository.findByAssignmentIn(assignments);
        List<QuizResult> quizzes = assignments.isEmpty() ? List.of() : quizResultRepository.findByAssignmentIn(assignments);
        Map<String, StudentProgress> progressByAssignmentAndStudent = new HashMap<>();
        savedProgress.forEach(progress -> progressByAssignmentAndStudent.put(progressKey(progress.getAssignment().getId(), progress.getStudentId()), progress));

        List<StudentAiStats> studentStats = enrollments.stream()
                .map(enrollment -> studentAiStats(enrollment.getStudentId(), assignments, progressByAssignmentAndStudent, quizzes))
                .toList();
        List<AssignmentAiStats> assignmentStats = assignments.stream()
                .map(assignment -> assignmentAiStats(assignment, enrollments, progressByAssignmentAndStudent, quizzes))
                .toList();
        long completed = studentStats.stream().mapToLong(StudentAiStats::completedAssignments).sum();
        long inProgress = studentStats.stream().mapToLong(StudentAiStats::inProgressAssignments).sum();
        long notStarted = studentStats.stream().mapToLong(StudentAiStats::notStartedAssignments).sum();
        List<String> algorithmsUsed = savedProgress.stream()
                .map(StudentProgress::getAlgorithm)
                .filter(algorithm -> algorithm != null)
                .map(Enum::name)
                .distinct()
                .toList();
        List<String> attention = studentStats.stream()
                .filter(student -> student.completedAssignments() == 0 || student.notStartedAssignments() > 0)
                .map(StudentAiStats::studentId)
                .distinct()
                .limit(10)
                .toList();
        return new ClassroomAiData(
                completed,
                notStarted,
                inProgress,
                algorithmsUsed,
                attention,
                studentStats,
                assignmentStats,
                stats
        );
    }

    private StudentAiStats studentAiStats(
            String studentId,
            List<Assignment> assignments,
            Map<String, StudentProgress> progressByAssignmentAndStudent,
            List<QuizResult> quizzes
    ) {
        long completed = 0;
        long inProgress = 0;
        long notStarted = 0;
        long totalRuns = 0;
        long totalComparisons = 0;
        long totalSwaps = 0;
        long totalSteps = 0;
        for (Assignment assignment : assignments) {
            StudentProgress progress = progressByAssignmentAndStudent.get(progressKey(assignment.getId(), studentId));
            ProgressStatus status = progress == null ? ProgressStatus.NOT_STARTED : progress.getStatus();
            if (status == ProgressStatus.COMPLETED) {
                completed++;
            } else if (status == ProgressStatus.IN_PROGRESS) {
                inProgress++;
            } else {
                notStarted++;
            }
            if (progress != null) {
                totalRuns += progress.getRunCount() == null ? 0 : progress.getRunCount();
                totalComparisons += progress.getTotalComparisons() == null ? 0 : progress.getTotalComparisons();
                totalSwaps += progress.getTotalSwaps() == null ? 0 : progress.getTotalSwaps();
                totalSteps += progress.getTotalSteps() == null ? 0 : progress.getTotalSteps();
            }
        }
        double averageScore = quizzes.stream()
                .filter(quiz -> quiz.getStudentId().equals(studentId))
                .mapToInt(quiz -> quiz.getScore() == null ? 0 : quiz.getScore())
                .average()
                .orElse(0);
        return new StudentAiStats(studentId, completed, inProgress, notStarted, totalRuns, totalComparisons, totalSwaps, totalSteps, averageScore);
    }

    private AssignmentAiStats assignmentAiStats(
            Assignment assignment,
            List<ClassEnrollment> enrollments,
            Map<String, StudentProgress> progressByAssignmentAndStudent,
            List<QuizResult> quizzes
    ) {
        long completed = 0;
        long inProgress = 0;
        long notStarted = 0;
        long totalRuns = 0;
        for (ClassEnrollment enrollment : enrollments) {
            StudentProgress progress = progressByAssignmentAndStudent.get(progressKey(assignment.getId(), enrollment.getStudentId()));
            ProgressStatus status = progress == null ? ProgressStatus.NOT_STARTED : progress.getStatus();
            if (status == ProgressStatus.COMPLETED) {
                completed++;
            } else if (status == ProgressStatus.IN_PROGRESS) {
                inProgress++;
            } else {
                notStarted++;
            }
            if (progress != null) {
                totalRuns += progress.getRunCount() == null ? 0 : progress.getRunCount();
            }
        }
        double averageScore = quizzes.stream()
                .filter(quiz -> quiz.getAssignment().getId().equals(assignment.getId()))
                .mapToInt(quiz -> quiz.getScore() == null ? 0 : quiz.getScore())
                .average()
                .orElse(0);
        return new AssignmentAiStats(
                assignment.getId(),
                assignment.getTitle(),
                assignment.getAlgorithm() == null ? "N/A" : assignment.getAlgorithm().name(),
                assignment.getDirection() == null ? "N/A" : assignment.getDirection().name(),
                assignment.getStatus().name(),
                completed,
                inProgress,
                notStarted,
                totalRuns,
                averageScore
        );
    }

    private AiAnalysisResponse aiAnalysisResponse(Long classId, String explanation, List<String> attention, ClassStatsResponse stats, ClassroomAiData aiData) {
        if (explanation == null || explanation.isBlank()) {
            return fallbackAiAnalysis(classId, attention, stats, aiData);
        }
        return new AiAnalysisResponse(
                classId,
                "Analiza este disponibila in campul fullAnalysis.",
                "Analiza este disponibila in campul fullAnalysis.",
                "Analiza este disponibila in campul fullAnalysis.",
                "Vezi sectiunea Recomandari pentru profesor din analiza completa.",
                attention,
                extractSection(explanation, "Concluzie"),
                explanation
        );
    }

    private AiAnalysisResponse fallbackAiAnalysis(Long classId, List<String> attention, ClassStatsResponse stats, ClassroomAiData aiData) {
        String fullAnalysis = deterministicClassAnalysis(stats, aiData);
        return new AiAnalysisResponse(
                classId,
                "Analiza este disponibila in campul fullAnalysis.",
                "Analiza este disponibila in campul fullAnalysis.",
                "Analiza este disponibila in campul fullAnalysis.",
                "Vezi sectiunea Recomandari pentru profesor din analiza completa.",
                attention,
                extractSection(fullAnalysis, "Concluzie"),
                fullAnalysis
        );
    }

    private String deterministicClassAnalysis(ClassStatsResponse stats, ClassroomAiData aiData) {
        String singleStudent = stats.studentCount() == 1 ? " Exista un singur elev in clasa, deci procentele trebuie interpretate la nivel individual." : "";
        String notStartedAssignments = aiData.assignmentStats().stream()
                .filter(assignment -> assignment.notStartedCount() > 0)
                .map(assignment -> assignment.title() + " (" + assignment.notStartedCount() + " neincepute)")
                .reduce((left, right) -> left + ", " + right)
                .orElse("Nu exista teme neincepute.");
        String inactiveStudents = aiData.studentStats().stream()
                .filter(student -> student.totalRuns() == 0)
                .map(StudentAiStats::studentId)
                .reduce((left, right) -> left + ", " + right)
                .orElse("Nu exista elevi fara activitate.");
        String scoreObservation = stats.averageQuizScore() >= 80
                ? "Scorul mediu este ridicat (" + String.format("%.1f", stats.averageQuizScore()) + "), ceea ce indica rezultate bune la quiz pentru datele existente."
                : "Scorul mediu este " + String.format("%.1f", stats.averageQuizScore()) + ", deci trebuie urmarite raspunsurile elevilor inainte de concluzii pozitive.";
        String completionImpact = stats.completionRate() < 50
                ? "Rata de finalizare este scazuta si poate ascunde nivelul real de intelegere, deoarece multe teme nu au rezultat final."
                : "Rata de finalizare permite interpretarea progresului pe baza unui numar suficient de rezultate finalizate.";

        return """
                Situatia clasei
                Clasa are %d elevi si %d teme.%s Statusurile agregate sunt: %d finalizate, %d in progres si %d neincepute.

                Performanta generala
                Rata de finalizare este %.1f%%, scorul mediu este %.1f, iar numarul total de rulari este %d. Algoritmii utilizati sunt: %s.

                Observatii importante
                %s Teme cu activitate neinceputa: %s

                Elevii care necesita atentie
                Elevi fara activitate: %s. Elevi marcati pentru atentie: %s.

                Recomandari pentru profesor
                Verifica mai intai temele neincepute si elevii fara rulari, apoi compara progresul pe fiecare tema cu scorurile quiz.

                Concluzie
                %s
                """.formatted(
                stats.studentCount(),
                stats.assignmentCount(),
                singleStudent,
                aiData.completedAssignments(),
                aiData.inProgressAssignments(),
                aiData.notStartedAssignments(),
                stats.completionRate(),
                stats.averageQuizScore(),
                stats.totalRuns(),
                aiData.algorithmsUsed().isEmpty() ? "N/A" : aiData.algorithmsUsed(),
                scoreObservation,
                notStartedAssignments,
                inactiveStudents,
                aiData.studentsNeedingAttention(),
                completionImpact
        );
    }

    private String extractSection(String text, String sectionName) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String[] lines = text.split("\\R");
        String normalized = sectionName.trim().toLowerCase();
        StringBuilder builder = new StringBuilder();
        boolean capturing = false;
        for (String line : lines) {
            String clean = line.replaceAll("^\\d+\\.\\s*", "").trim();
            if (clean.equalsIgnoreCase(sectionName)) {
                capturing = true;
                continue;
            }
            if (capturing && isClassAnalysisSection(clean) && !clean.toLowerCase().equals(normalized)) {
                break;
            }
            if (capturing && !clean.isBlank()) {
                if (builder.length() > 0) {
                    builder.append(' ');
                }
                builder.append(clean);
            }
        }
        return builder.length() == 0 ? text : builder.toString();
    }

    private boolean isClassAnalysisSection(String line) {
        String value = line.toLowerCase();
        return value.equals("situatia clasei")
                || value.equals("performanta generala")
                || value.equals("observatii importante")
                || value.equals("elevii care necesita atentie")
                || value.equals("recomandari pentru profesor")
                || value.equals("concluzie");
    }

    private record ClassroomAiData(
            long completedAssignments,
            long notStartedAssignments,
            long inProgressAssignments,
            List<String> algorithmsUsed,
            List<String> studentsNeedingAttention,
            List<StudentAiStats> studentStats,
            List<AssignmentAiStats> assignmentStats,
            ClassStatsResponse stats
    ) {}

    private record StudentAiStats(
            String studentId,
            long completedAssignments,
            long inProgressAssignments,
            long notStartedAssignments,
            long totalRuns,
            long totalComparisons,
            long totalSwaps,
            long totalSteps,
            double averageQuizScore
    ) {}

    private record AssignmentAiStats(
            Long assignmentId,
            String title,
            String algorithm,
            String direction,
            String status,
            long completedCount,
            long inProgressCount,
            long notStartedCount,
            long totalRuns,
            double averageQuizScore
    ) {}

    private byte[] minimalPdf(String text) {
        String escaped = text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)").replace("\n", "\\n");
        String pdf = "%PDF-1.4\n"
                + "1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n"
                + "2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n"
                + "3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >> endobj\n"
                + "4 0 obj << /Length " + (escaped.length() + 64) + " >> stream\n"
                + "BT /F1 12 Tf 50 790 Td (" + escaped + ") Tj ET\n"
                + "endstream endobj\n"
                + "5 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n"
                + "xref\n0 6\n0000000000 65535 f \n"
                + "trailer << /Root 1 0 R /Size 6 >>\nstartxref\n0\n%%EOF";
        return pdf.getBytes(StandardCharsets.UTF_8);
    }
}
