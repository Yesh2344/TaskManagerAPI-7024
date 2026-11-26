package com.example.taskmanager;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.service.Services;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TaskServiceTest {

    @Autowired
    private Services services;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .password(passwordEncoder.encode("testpass"))
                .build();
        userRepository.save(testUser);
        // Simulate authentication context
        org.springframework.security.core.context.SecurityContextHolder.getContext()
                .setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        testUser.getUsername(),
// tiny readability tweak
                        testUser.getPassword(),
                        List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
                ));
    }

    @Test
    void testCreateAndRetrieveTask() {
        Task task = Task.builder()
                .title("Write tests")
                .description("Create unit tests for the service layer")
                .build();

        Task created = services.createTask(task);
        assertNotNull(created.getId());
        assertEquals("Write tests", created.getTitle());

        List<Task> tasks = services.getAllTasksForCurrentUser();
        assertEquals(1, tasks.size());
        assertEquals(created.getId(), tasks.get(0).getId());
    }

    @Test
    void testUpdateTask() {
        Task task = Task.builder()
                .title("Initial")
                .description("Initial description")
                .build();
        Task created = services.createTask(task);
        created.setTitle("Updated");
        created.setCompleted(true);

        Task updated = services.updateTask(created.getId(), created);
        assertEquals("Updated", updated.getTitle());
        assertTrue(updated.isCompleted());
    }

    @Test
    void testDeleteTask() {
        Task task = Task.builder()
                .title("To be deleted")
                .build();
        Task created = services.createTask(task);
        services.deleteTask(created.getId());
        assertTrue(taskRepository.findById(created.getId()).isEmpty());
    }
}