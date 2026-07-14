package com.example.demo.config;

import com.example.demo.model.AppUser;
import com.example.demo.model.Role;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(2)
public class DefaultProfilesInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    public DefaultProfilesInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        List<SeedProfile> profiles = List.of(
                new SeedProfile("Alice", "Admin", "alice.admin@example.com", "+15550000001", "123 Admin St, Tech City", Role.ADMIN),
                new SeedProfile("Bob", "Builder", "bob.student@example.com", "+15550000002", "42 Campus Dr, College Town", Role.STUDENT),
                new SeedProfile("Carol", "Coder", "carol.prof@example.com", "+15550000003", "9 Faculty Rd, University", Role.PROFESOR),
                new SeedProfile("Dave", "Data", "dave.student@example.com", "+15550000004", "77 Library Ln, College Town", Role.STUDENT),
                new SeedProfile("Eve", "Engineer", "eve.admin@example.com", "+15550000005", "500 Security Blvd, Tech City", Role.ADMIN),
                new SeedProfile("Frank", "Faculty", "frank.prof@example.com", "+15550000006", "11 Lecture Hall, University", Role.PROFESOR)
        );

        profiles.forEach(this::upsertProfile);
    }

    private void upsertProfile(SeedProfile seed) {
        AppUser user = userRepository.findByEmail(seed.email()).orElseGet(AppUser::new);
        user.setFirstName(seed.firstName());
        user.setLastName(seed.lastName());
        user.setEmail(seed.email());
        user.setPhoneNumber(seed.phoneNumber());
        user.setAddress(seed.address());
        user.setRole(seed.role());
        userRepository.save(user);
    }

    private record SeedProfile(String firstName, String lastName, String email, String phoneNumber, String address, Role role) {
    }
}
