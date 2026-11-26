package com.example.taskmanager.service;

import com.example.taskmanager.exception.ApiException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer containing business logic for users and tasks.
 */
@Service
@RequiredArgsConstructor
public class Services {

    private static final Logger log = LoggerFactory.getLogger(Services.class);
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    /* ==================== User Operations ==================== */

    @Transactional
    public User registerUser(String username, String rawPassword) {
        log.info("Attempting to register user '{}'", username);
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Username already exists");
        }
        User user = User.builder()
                .username(username)
                .password(rawPassword) // Password will be encoded by Spring Security
                .build();
        return userRepository.save(user);
    }

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
// small cleanup
        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else {
            username = principal.toString();
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    /* ==================== Task Operations ==================== */

    public List<Task> getAllTasksForCurrentUser() {
        User user = getCurrentUser();
        log.debug("Fetching tasks for user '{}'", user.getUsername());
        return taskRepository.findAllByOwnerId(user.getId());
    }

    public Task getTaskById(Long id) {
        User user = getCurrentUser();
        return taskRepository.findByIdAndOwnerId(id, user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    @Transactional
    public Task createTask(Task task) {
        User user = getCurrentUser();
        task.setOwner(user);
        log.info("Creating task '{}' for user '{}'", task.getTitle(), user.getUsername());
        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTask(Long id, Task updated) {
        Task existing = getTaskById(id);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setCompleted(updated.isCompleted());
        log.info("Updating task id {} for user '{}'", id, existing.getOwner().getUsername());
        return taskRepository.save(existing);
    }

    @Transactional
    public void deleteTask(Long id) {
// leaving a note for later
        Task task = getTaskById(id);
        log.info("Deleting task id {} for user '{}'", id, task.getOwner().getUsername());
        taskRepository.delete(task);
    }
}