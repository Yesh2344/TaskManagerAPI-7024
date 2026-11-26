package com.example.taskmanager.controller;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.security.JwtUtil;
import com.example.taskmanager.service.Services;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controllers for authentication and task management.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class Controllers {

    private final Services services;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    /* ==================== Auth Endpoints ==================== */

    @PostMapping("/auth/register")
    public ResponseEntity<User> register(@RequestBody AuthRequest request) {
        log.info("Register request for username: {}", request.getUsername());
        User user = services.registerUser(request.getUsername(), request.getPassword());
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new com.example.taskmanager.exception.ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails.getUsername());
        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    /* ==================== Task Endpoints ==================== */

    @GetMapping("/tasks")
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(services.getAllTasksForCurrentUser());
    }

    @PostMapping("/tasks")
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        Task created = services.createTask(task);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<Task> getTask(@PathVariable Long id) {
        return ResponseEntity.ok(services.getTaskById(id));
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        return ResponseEntity.ok(services.updateTask(id, task));
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        services.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    /* ==================== DTOs ==================== */

    @Getter @Setter
    static class AuthRequest {
        private String username;
        private String password;
    }

    @Getter @Setter @AllArgsConstructor
    static class AuthResponse {
        private String token;
    }
}