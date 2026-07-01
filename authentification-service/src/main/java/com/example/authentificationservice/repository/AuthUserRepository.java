package com.example.authentificationservice.repository;

import com.example.authentificationservice.model.AuthUser;
import com.example.authentificationservice.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {
    Optional<AuthUser> findByEmail(String email);

    boolean existsByEmail(String email);

    List<AuthUser> findByRoleInOrderByLastNameAscFirstNameAscEmailAsc(List<Role> roles);
}
