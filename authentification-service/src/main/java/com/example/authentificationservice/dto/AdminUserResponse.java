package com.example.authentificationservice.dto;

public record AdminUserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String role
) {
}
