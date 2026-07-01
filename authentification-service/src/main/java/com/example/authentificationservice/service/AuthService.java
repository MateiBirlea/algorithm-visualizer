package com.example.authentificationservice.service;

import com.example.authentificationservice.dto.AdminCreateUserRequest;
import com.example.authentificationservice.dto.AdminUserResponse;
import com.example.authentificationservice.dto.AuthResponse;
import com.example.authentificationservice.dto.LoginRequest;
import com.example.authentificationservice.dto.RegisterRequest;
import com.example.authentificationservice.model.AuthUser;
import com.example.authentificationservice.model.Role;
import com.example.authentificationservice.repository.AuthUserRepository;
import com.example.authentificationservice.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AuthService {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ApplicationUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthService(
            AuthUserRepository authUserRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            ApplicationUserDetailsService userDetailsService,
            JwtService jwtService
    ) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (authUserRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already used");
        }
        Role requestedRole = request.role() == null ? Role.STUDENT : request.role();
        if (requestedRole == Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin users can only be created by an existing admin");
        }

        AuthUser user = new AuthUser();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(requestedRole);
        authUserRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);
        return buildResponse(user, token);
    }

    public List<AdminUserResponse> listManagedUsers() {
        return authUserRepository
                .findByRoleInOrderByLastNameAscFirstNameAscEmailAsc(List.of(Role.STUDENT, Role.PROFESOR))
                .stream()
                .map(this::toAdminUserResponse)
                .toList();
    }

    public AdminUserResponse createManagedUser(AdminCreateUserRequest request) {
        if (request.role() == Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin can only create STUDENT or PROFESOR accounts");
        }
        if (authUserRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already used");
        }

        AuthUser user = new AuthUser();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        authUserRepository.save(user);

        return toAdminUserResponse(user);
    }

    public void deleteManagedUser(Long id) {
        AuthUser user = authUserRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getRole() == Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin accounts cannot be deleted from this panel");
        }
        authUserRepository.delete(user);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        AuthUser user = authUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);
        return buildResponse(user, token);
    }

    private AuthResponse buildResponse(AuthUser user, String token) {
        return new AuthResponse(
                token,
                jwtService.getExpirationMs(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name()
        );
    }

    private AdminUserResponse toAdminUserResponse(AuthUser user) {
        return new AdminUserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
