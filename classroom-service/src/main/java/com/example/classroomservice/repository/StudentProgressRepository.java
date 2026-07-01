package com.example.classroomservice.repository;

import com.example.classroomservice.model.Assignment;
import com.example.classroomservice.model.ProgressStatus;
import com.example.classroomservice.model.StudentProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StudentProgressRepository extends JpaRepository<StudentProgress, Long> {
    Optional<StudentProgress> findByAssignmentAndStudentId(Assignment assignment, String studentId);

    List<StudentProgress> findByAssignmentOrderByStudentIdAsc(Assignment assignment);

    List<StudentProgress> findByStudentIdOrderByStartedAtDesc(String studentId);

    List<StudentProgress> findByAssignmentIn(Collection<Assignment> assignments);

    long countByAssignmentInAndStatus(Collection<Assignment> assignments, ProgressStatus status);

    void deleteByAssignment(Assignment assignment);

    void deleteByAssignmentIn(Collection<Assignment> assignments);
}
