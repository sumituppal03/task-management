package com.taskmanagement.taskmanagement.service;

// JUnit 5 imports — the testing framework
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

// Mockito imports — for creating fake objects
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// Spring Data imports — for pagination
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

// Your project imports
import com.taskmanagement.taskmanagement.dto.TaskRequest;
import com.taskmanagement.taskmanagement.dto.TaskResponse;
import com.taskmanagement.taskmanagement.exception.ResourceNotFoundException;
import com.taskmanagement.taskmanagement.exception.UnauthorizedException;
import com.taskmanagement.taskmanagement.model.Task;
import com.taskmanagement.taskmanagement.model.User;
import com.taskmanagement.taskmanagement.repository.TaskRepository;
import com.taskmanagement.taskmanagement.repository.UserRepository;

// Java imports
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

// Static imports — makes test code cleaner
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private TaskService taskService;
    
    private User testUser;
    private Task testTask;
    private TaskRequest taskRequest;

    @BeforeEach
    void setUp() {
       
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Sumit Uppal");
        testUser.setEmail("sumit@gmail.com");
        testUser.setRole(User.Role.USER);

        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Fix critical bug");
        testTask.setDescription("Bug in payment module");
        testTask.setPriority(Task.Priority.HIGH);
        testTask.setStatus(Task.Status.TODO);
        testTask.setDueDate(LocalDate.now().plusDays(5));
        testTask.setUser(testUser);
        testTask.setCreatedAt(LocalDateTime.now());
        testTask.setUpdatedAt(LocalDateTime.now());

        taskRequest = new TaskRequest();
        taskRequest.setTitle("Fix critical bug");
        taskRequest.setDescription("Bug in payment module");
        taskRequest.setPriority(Task.Priority.HIGH);
        taskRequest.setDueDate(LocalDate.now().plusDays(5));
    }

    @Test
    @DisplayName("Create task successfully when user exists")
    void createTask_WithValidUser_ReturnsTaskResponse() {

        when(userRepository.findByEmail("sumit@gmail.com")).thenReturn(Optional.of(testUser));

        
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        TaskResponse response = taskService.createTask(taskRequest, "sumit@gmail.com");

       
        assertNotNull(response);
        assertEquals("Fix critical bug", response.getTitle());
        assertEquals("HIGH", response.getPriority());

        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Create task fails when user does not exist")
    void createTask_WithInvalidUser_ThrowsResourceNotFoundException() {

        when(userRepository.findByEmail("notexist@gmail.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->taskService.createTask(taskRequest,"notexist@gmail.com"));

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Get task by ID successfully when task exists and user owns it")
    void getTaskById_WithValidIdAndOwner_ReturnsTask() {

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(testTask));

        TaskResponse response = taskService
                .getTaskById(1L, "sumit@gmail.com");

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Fix critical bug", response.getTitle());
    }

    @Test
    @DisplayName("Get task throws exception when task not found")
    void getTaskById_WithInvalidId_ThrowsResourceNotFoundException() {


        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->taskService.getTaskById(999L, "sumit@gmail.com"));
    }

    @Test
    @DisplayName("Get task throws exception when user does not own the task")
    void getTaskById_WithWrongUser_ThrowsUnauthorizedException() {

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        assertThrows(UnauthorizedException.class, () ->taskService.getTaskById(1L, "hacker@gmail.com"));

    }

    @Test
    @DisplayName("Update task successfully when user owns it")
    void updateTask_WithValidOwner_ReturnsUpdatedTask() {

        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setDescription("Updated description");
        updateRequest.setPriority(Task.Priority.URGENT);
        updateRequest.setDueDate(LocalDate.now().plusDays(3));

        Task updatedTask = new Task();
        updatedTask.setId(1L);
        updatedTask.setTitle("Updated Title");
        updatedTask.setPriority(Task.Priority.URGENT);
        updatedTask.setStatus(Task.Status.TODO);
        updatedTask.setUser(testUser);
        updatedTask.setCreatedAt(LocalDateTime.now());
        updatedTask.setUpdatedAt(LocalDateTime.now());

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        TaskResponse response = taskService.updateTask(1L, updateRequest, "sumit@gmail.com");

        assertNotNull(response);
        assertEquals("Updated Title", response.getTitle());
        assertEquals("URGENT", response.getPriority());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Update task throws exception when wrong user tries to update")
    void updateTask_WithWrongUser_ThrowsUnauthorizedException() {

        // ── ARRANGE ──────────────────────────────────────────
        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(testTask));

        // ── ACT + ASSERT ──────────────────────────────────────
        // attacker@gmail.com tries to update sumit's task
        assertThrows(UnauthorizedException.class, () ->
                taskService.updateTask(1L, taskRequest,
                        "attacker@gmail.com"));

        // Most important: verify save was NEVER called
        // Task must not be saved if wrong user!
        verify(taskRepository, never()).save(any(Task.class));
    }

    // ═══════════════════════════════════════════════════════════
    // DELETE TASK TESTS
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Delete task successfully when user owns it")
    void deleteTask_WithValidOwner_DeletesTask() {

        // ── ARRANGE ──────────────────────────────────────────
        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(testTask));

        // ── ACT ───────────────────────────────────────────────
        // deleteTask returns void — no response to check
        taskService.deleteTask(1L, "sumit@gmail.com");

        // ── ASSERT ────────────────────────────────────────────
        // Verify delete was called with the correct task
        verify(taskRepository, times(1)).delete(testTask);
        // This confirms TaskService actually called delete
        // Not just returned without deleting!
    }

    @Test
    @DisplayName("Delete task throws exception when wrong user tries to delete")
    void deleteTask_WithWrongUser_ThrowsUnauthorizedException() {

        // ── ARRANGE ──────────────────────────────────────────
        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(testTask));

        // ── ACT + ASSERT ──────────────────────────────────────
        assertThrows(UnauthorizedException.class, () ->
                taskService.deleteTask(1L, "attacker@gmail.com"));

        // Delete must NEVER be called for wrong user!
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    @DisplayName("Delete task throws exception when task not found")
    void deleteTask_WithInvalidId_ThrowsResourceNotFoundException() {

        // ── ARRANGE ──────────────────────────────────────────
        when(taskRepository.findById(999L))
                .thenReturn(Optional.empty());

        // ── ACT + ASSERT ──────────────────────────────────────
        assertThrows(ResourceNotFoundException.class, () ->
                taskService.deleteTask(999L, "sumit@gmail.com"));

        verify(taskRepository, never()).delete(any(Task.class));
    }

    // GET MY TASKS TESTS (PAGINATION)

    @Test
    @DisplayName("Get my tasks returns paginated list for valid user")
    void getMyTasks_WithValidUser_ReturnsPaginatedTasks() {

        // Create a page of tasks (simulates paginated DB result)
        List<Task> taskList = Arrays.asList(testTask);
        Page<Task> taskPage = new PageImpl<>(taskList,PageRequest.of(0, 10), 1);
        when(userRepository.findByEmail("sumit@gmail.com"))
                .thenReturn(Optional.of(testUser));
        when(taskRepository.findByUserId(
                eq(1L), any(Pageable.class)))
                .thenReturn(taskPage);

        // ACT 
        Page<TaskResponse> result = taskService
                .getMyTasks("sumit@gmail.com", 0, 10, "createdAt");

        // ASSERT 
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        // getTotalElements = total count across all pages
        assertEquals(1, result.getContent().size());
        // getContent = tasks on this page
    }

    // SEARCH TASKS TESTS


    @Test
    @DisplayName("Search tasks returns matching tasks for keyword")
    void searchTasks_WithValidKeyword_ReturnsMatchingTasks() {

        // ── ARRANGE ──────────────────────────────────────────
        when(userRepository.findByEmail("sumit@gmail.com"))
                .thenReturn(Optional.of(testUser));
        when(taskRepository
                .findByUserIdAndTitleContainingIgnoreCase(
                        1L, "bug"))
                .thenReturn(Arrays.asList(testTask));

        // ACT 
        List<TaskResponse> results = taskService
                .searchTasks("sumit@gmail.com", "bug");

        // ── ASSERT ────────────────────────────────────────────
        assertNotNull(results);
        assertFalse(results.isEmpty());
        // assertFalse = condition must be false (list not empty)
        assertEquals(1, results.size());
        assertEquals("Fix critical bug",
                results.get(0).getTitle());
    }

    @Test
    @DisplayName("Search tasks returns empty list when no match")
    void searchTasks_WithNoMatch_ReturnsEmptyList() {

        // ── ARRANGE ──────────────────────────────────────────
        when(userRepository.findByEmail("sumit@gmail.com"))
                .thenReturn(Optional.of(testUser));
        when(taskRepository
                .findByUserIdAndTitleContainingIgnoreCase(
                        1L, "xyz"))
                .thenReturn(Arrays.asList());
        // Empty list = no tasks match "xyz"

        // ── ACT ───────────────────────────────────────────────
        List<TaskResponse> results = taskService
                .searchTasks("sumit@gmail.com", "xyz");

        // ── ASSERT ────────────────────────────────────────────
        assertNotNull(results);
        assertTrue(results.isEmpty());
        // assertTrue = condition must be true (list IS empty)
    }



    @Test
    @DisplayName("Get overdue tasks returns tasks past due date")
    void getOverdueTasks_ReturnsOverdueTasks() {

        // ── ARRANGE ──────────────────────────────────────────
        // Create a task that IS overdue
        Task overdueTask = new Task();
        overdueTask.setId(2L);
        overdueTask.setTitle("Overdue Task");
        overdueTask.setPriority(Task.Priority.URGENT);
        overdueTask.setStatus(Task.Status.TODO);
        overdueTask.setDueDate(LocalDate.now().minusDays(3));
        // minusDays(3) = 3 days in the PAST = overdue!
        overdueTask.setUser(testUser);
        overdueTask.setCreatedAt(LocalDateTime.now());
        overdueTask.setUpdatedAt(LocalDateTime.now());

        when(userRepository.findByEmail("sumit@gmail.com"))
                .thenReturn(Optional.of(testUser));
        when(taskRepository.findOverdueTasks(
                eq(1L), any(LocalDate.class)))
                .thenReturn(Arrays.asList(overdueTask));

        // ── ACT ───────────────────────────────────────────────
        List<TaskResponse> results = taskService
                .getOverdueTasks("sumit@gmail.com");

        // ── ASSERT ────────────────────────────────────────────
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.get(0).isOverdue());
        
    }

    
    @Test
    @DisplayName("Get task stats returns correct counts")
    void getTaskStats_ReturnsCorrectStatistics() {

        // ── ARRANGE ──────────────────────────────────────────
        when(userRepository.findByEmail("sumit@gmail.com"))
                .thenReturn(Optional.of(testUser));
        when(taskRepository.count())
                .thenReturn(10L);
        when(taskRepository.countByUserIdAndStatus(
                1L, Task.Status.TODO))
                .thenReturn(4L);
        when(taskRepository.countByUserIdAndStatus(
                1L, Task.Status.IN_PROGRESS))
                .thenReturn(3L);
        when(taskRepository.countByUserIdAndStatus(
                1L, Task.Status.COMPLETED))
                .thenReturn(3L);
        when(taskRepository.findOverdueTasks(
                eq(1L), any(LocalDate.class)))
                .thenReturn(Arrays.asList(testTask));

        // ── ACT ───────────────────────────────────────────────
        var stats = taskService.getTaskStats("sumit@gmail.com");
        // var = Java type inference (Map<String, Long>)

        // ── ASSERT ────────────────────────────────────────────
        assertNotNull(stats);
        assertEquals(10L, stats.get("total"));
        assertEquals(4L, stats.get("todo"));
        assertEquals(3L, stats.get("inProgress"));
        assertEquals(3L, stats.get("completed"));
        assertEquals(1L, stats.get("overdue"));
    }
}