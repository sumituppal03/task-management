package com.taskmanagement.taskmanagement.service;

import com.taskmanagement.taskmanagement.dto.TaskRequest;
import com.taskmanagement.taskmanagement.dto.TaskResponse;
import com.taskmanagement.taskmanagement.exception.ResourceNotFoundException;
import com.taskmanagement.taskmanagement.exception.UnauthorizedException;
import com.taskmanagement.taskmanagement.model.Task;
import com.taskmanagement.taskmanagement.model.User;
import com.taskmanagement.taskmanagement.repository.TaskRepository;
import com.taskmanagement.taskmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    // CREATE task
    public TaskResponse createTask(TaskRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", email));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        task.setUser(user);

        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }

        Task saved = taskRepository.save(task);
        return convertToResponse(saved);
    }

    // GET all tasks for logged in user with pagination
    public Page<TaskResponse> getMyTasks(
            String email, int page, int size, String sortBy) {

        User user = getUserByEmail(email);
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(sortBy).descending());

        return taskRepository
                .findByUserId(user.getId(), pageable)
                .map(this::convertToResponse);
    }

    // GET tasks filtered by status
    public Page<TaskResponse> getTasksByStatus(
            String email, Task.Status status,
            int page, int size) {

        User user = getUserByEmail(email);
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        return taskRepository
                .findByUserIdAndStatus(
                    user.getId(), status, pageable)
                .map(this::convertToResponse);
    }

    // GET tasks filtered by priority
    public Page<TaskResponse> getTasksByPriority(
            String email, Task.Priority priority,
            int page, int size) {

        User user = getUserByEmail(email);
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        return taskRepository
                .findByUserIdAndPriority(
                    user.getId(), priority, pageable)
                .map(this::convertToResponse);
    }

    // SEARCH tasks by title keyword
    public List<TaskResponse> searchTasks(
            String email, String keyword) {

        User user = getUserByEmail(email);
        return taskRepository
                .findByUserIdAndTitleContainingIgnoreCase(
                    user.getId(), keyword)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // GET single task by id
    public TaskResponse getTaskById(Long taskId, String email) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task", "id", taskId));

        verifyTaskOwnership(task, email);
        return convertToResponse(task);
    }

    // UPDATE task
    public TaskResponse updateTask(
            Long taskId, TaskRequest request, String email) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task", "id", taskId));

        verifyTaskOwnership(task, email);

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());

        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }

        Task updated = taskRepository.save(task);
        return convertToResponse(updated);
    }

    // DELETE task
    public void deleteTask(Long taskId, String email) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task", "id", taskId));

        verifyTaskOwnership(task, email);
        taskRepository.delete(task);
    }

    // GET overdue tasks
    public List<TaskResponse> getOverdueTasks(String email) {
        User user = getUserByEmail(email);
        return taskRepository
                .findOverdueTasks(user.getId(), LocalDate.now())
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // GET task statistics
    public Map<String, Long> getTaskStats(String email) {
        User user = getUserByEmail(email);
        Long userId = user.getId();

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", taskRepository.count());
        stats.put("todo",
            taskRepository.countByUserIdAndStatus(
                userId, Task.Status.TODO));
        stats.put("inProgress",
            taskRepository.countByUserIdAndStatus(
                userId, Task.Status.IN_PROGRESS));
        stats.put("completed",
            taskRepository.countByUserIdAndStatus(
                userId, Task.Status.COMPLETED));
        stats.put("overdue",
            (long) taskRepository.findOverdueTasks(
                userId, LocalDate.now()).size());

        return stats;
    }

    // HELPER — verify user owns the task
    private void verifyTaskOwnership(Task task, String email) {
        if (!task.getUser().getEmail().equals(email)) {
            throw new UnauthorizedException(
                "You don't have permission to access this task");
        }
    }

    // HELPER — get user by email
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", email));
    }

    // CONVERT Task to TaskResponse DTO
    private TaskResponse convertToResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setPriority(task.getPriority().name());
        response.setStatus(task.getStatus().name());
        response.setDueDate(task.getDueDate());
        response.setOwnerName(task.getUser().getName());
        response.setOwnerEmail(task.getUser().getEmail());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());

        response.setOverdue(
            task.getDueDate() != null &&
            task.getDueDate().isBefore(LocalDate.now()) &&
            task.getStatus() != Task.Status.COMPLETED
        );

        return response;
    }
}