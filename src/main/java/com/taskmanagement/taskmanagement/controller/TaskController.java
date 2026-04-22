package com.taskmanagement.taskmanagement.controller;

import com.taskmanagement.taskmanagement.dto.TaskRequest;
import com.taskmanagement.taskmanagement.dto.TaskResponse;
import com.taskmanagement.taskmanagement.model.Task;
import com.taskmanagement.taskmanagement.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal String email) {
        TaskResponse response = taskService
            .createTask(request, email);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getMyTasks(
            @AuthenticationPrincipal String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt")
                String sortBy) {
        return ResponseEntity.ok(
            taskService.getMyTasks(email, page, size, sortBy));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<TaskResponse>> getByStatus(
            @AuthenticationPrincipal String email,
            @PathVariable Task.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
            taskService.getTasksByStatus(
                email, status, page, size));
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<Page<TaskResponse>> getByPriority(
            @AuthenticationPrincipal String email,
            @PathVariable Task.Priority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
            taskService.getTasksByPriority(
                email, priority, page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<List<TaskResponse>> search(
            @AuthenticationPrincipal String email,
            @RequestParam String keyword) {
        return ResponseEntity.ok(
            taskService.searchTasks(email, keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(
            taskService.getTaskById(id, email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(
            taskService.updateTask(id, request, email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal String email) {
        taskService.deleteTask(id, email);
        return ResponseEntity.ok("Task deleted successfully");
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<TaskResponse>> getOverdue(
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(
            taskService.getOverdueTasks(email));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats(
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(
            taskService.getTaskStats(email));
    }
}
