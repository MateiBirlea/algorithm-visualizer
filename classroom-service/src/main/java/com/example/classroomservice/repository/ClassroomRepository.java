package com.example.classroomservice.repository;

import com.example.classroomservice.model.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    List<Classroom> findByTeacherIdOrderByCreatedAtDesc(String teacherId);

    Optional<Classroom> findByJoinCode(String joinCode);

    boolean existsByJoinCode(String joinCode);
}
