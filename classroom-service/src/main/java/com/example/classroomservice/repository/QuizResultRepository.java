package com.example.classroomservice.repository;

import com.example.classroomservice.model.Assignment;
import com.example.classroomservice.model.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    List<QuizResult> findByAssignmentOrderByCreatedAtDesc(Assignment assignment);

    List<QuizResult> findByStudentIdOrderByCreatedAtDesc(String studentId);

    List<QuizResult> findByAssignmentIn(Collection<Assignment> assignments);

    void deleteByAssignment(Assignment assignment);

    void deleteByAssignmentIn(Collection<Assignment> assignments);
}
