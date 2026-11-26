package com.example.taskmanager.repository;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link User} entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}

/**
 * Repository for {@link Task} entities.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByOwnerId(Long ownerId);
    Optional<Task> findByIdAndOwnerId(Long id, Long ownerId);
}