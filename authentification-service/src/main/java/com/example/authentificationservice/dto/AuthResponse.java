package com.example.authentificationservice.dto;

public record AuthResponse(
        String token,
        long expiresIn,
        String email,
        String firstName,
        String lastName,
        String role
) {
}
