package com.example.classroomservice.repository;

import com.example.classroomservice.model.ClassEnrollment;
import com.example.classroomservice.model.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassEnrollmentRepository extends JpaRepository<ClassEnrollment, Long> {
    List<ClassEnrollment> findByStudentIdOrderByEnrolledAtDesc(String studentId);

    List<ClassEnrollment> findByClassroomOrderByEnrolledAtAsc(Classroom classroom);

    boolean existsByClassroomAndStudentId(Classroom classroom, String studentId);

    long countByClassroom(Classroom classroom);

    Optional<ClassEnrollment> findByClassroomAndStudentId(Classroom classroom, String studentId);

    void deleteByClassroom(Classroom classroom);
}
