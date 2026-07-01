package com.example.authentificationservice.config;

import com.example.authentificationservice.model.AuthUser;
import com.example.authentificationservice.model.Role;
import com.example.authentificationservice.repository.AuthUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds a few users for local/testing environments so login works out of the box.
 * Uses the same bcrypt encoder as authentication.
 */
@Component
public class DefaultUsersInitializer implements CommandLineRunner {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultUsersInitializer(AuthUserRepository authUserRepository, PasswordEncoder passwordEncoder) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        List<SeedUser> seeds = List.of(
                new SeedUser("Alice", "Admin", "alice.admin@example.com", "Admin", Role.ADMIN),
                new SeedUser("Bob", "Builder", "bob.student@example.com", "Student123", Role.STUDENT),
                new SeedUser("Carol", "Coder", "carol.prof@example.com", "Professor123", Role.PROFESOR)
        );

        seeds.forEach(this::createIfMissing);
    }

    private void createIfMissing(SeedUser seed) {
        if (authUserRepository.existsByEmail(seed.email)) {
            return;
        }

        AuthUser user = new AuthUser();
        user.setFirstName(seed.firstName);
        user.setLastName(seed.lastName);
        user.setEmail(seed.email);
        user.setPassword(passwordEncoder.encode(seed.rawPassword));
        user.setRole(seed.role);
        authUserRepository.save(user);
    }

    private record SeedUser(String firstName, String lastName, String email, String rawPassword, Role role) {}
}
