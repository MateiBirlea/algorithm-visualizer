package com.example.classroomservice.controller;

import com.example.classroomservice.dto.ClassroomDtos.ClassroomRequest;
import com.example.classroomservice.dto.ClassroomDtos.ClassroomResponse;
import com.example.classroomservice.dto.ClassroomDtos.EnrollmentResponse;
import com.example.classroomservice.dto.ClassroomDtos.JoinClassRequest;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
public class ClassroomController {
    private final ClassroomService classroomService;

    public ClassroomController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PROFESOR')")
    public ClassroomResponse createClassroom(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody ClassroomRequest request) {
        return classroomService.createClassroom(user.getEmail(), request);
    }

    @GetMapping("/my")
    public List<ClassroomResponse> myClasses(@AuthenticationPrincipal AuthenticatedUser user) {
        boolean teacher = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PROFESOR"));
        return teacher ? classroomService.myTeacherClasses(user.getEmail()) : classroomService.myStudentClasses(user.getEmail());
    }

    @GetMapping("/{classId}")
    public ClassroomResponse getClassroom(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long classId) {
        return classroomService.getClassroom(user.getEmail(), classId);
    }

    @PutMapping("/{classId}")
    @PreAuthorize("hasRole('PROFESOR')")
    public ClassroomResponse updateClassroom(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long classId,
            @Valid @RequestBody ClassroomRequest request
    ) {
        return classroomService.updateClassroom(user.getEmail(), classId, request);
    }

    @DeleteMapping("/{classId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('PROFESOR')")
    public void deleteClassroom(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long classId) {
        classroomService.deleteClassroom(user.getEmail(), classId);
    }

    @PostMapping("/join")
    @PreAuthorize("hasRole('STUDENT')")
    public ClassroomResponse joinClass(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody JoinClassRequest request) {
        return classroomService.joinClass(user.getEmail(), request.joinCode());
    }

    @PostMapping("/{classId}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('STUDENT')")
    public void leaveClass(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long classId) {
        classroomService.leaveClass(user.getEmail(), classId);
    }

    @GetMapping("/{classId}/students")
    @PreAuthorize("hasRole('PROFESOR')")
    public List<EnrollmentResponse> classStudents(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long classId) {
        return classroomService.classStudents(user.getEmail(), classId);
    }
}
