package com.example.classroomservice.repository;

import com.example.classroomservice.model.Assignment;
import com.example.classroomservice.model.AssignmentStatus;
import com.example.classroomservice.model.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByClassroomOrderByCreatedAtDesc(Classroom classroom);

    List<Assignment> findByClassroomAndStatusOrderByCreatedAtDesc(Classroom classroom, AssignmentStatus status);

    long countByClassroom(Classroom classroom);

    long countByTeacherId(String teacherId);

    long countByTeacherIdAndStatus(String teacherId, AssignmentStatus status);

    List<Assignment> findByTeacherId(String teacherId);
}
