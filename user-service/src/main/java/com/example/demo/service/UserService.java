package com.example.demo.service;

import com.example.demo.model.AppUser;
import com.example.demo.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<AppUser> findAll() {
        return userRepository.findAll();
    }

    public AppUser findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public AppUser create(AppUser user) {
        user.setId(null);
        return userRepository.save(user);
    }

    public AppUser update(Long id, AppUser user) {
        AppUser existingUser = findById(id);
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhoneNumber(user.getPhoneNumber());
        existingUser.setAddress(user.getAddress());
        existingUser.setRole(user.getRole());
        return userRepository.save(existingUser);
    }

    public void delete(Long id) {
        AppUser existingUser = findById(id);
        userRepository.delete(existingUser);
    }
}
