package com.taskmanagement.taskmanagement.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Tasks",
     description = "Task management endpoints. All require JWT token.")

@SecurityRequirement(name = "Bearer Authentication")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping
    @Operation(
        summary = "Create a new task",
        description = "Creates a task for the currently logged-in user. " +
                      "User is identified from JWT token automatically."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201",
                     description = "Task created successfully"),
        @ApiResponse(responseCode = "400",
                     description = "Validation failed"),
        @ApiResponse(responseCode = "401",
                     description = "JWT token missing or invalid")
    })
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal String email) {
        TaskResponse response = taskService
            .createTask(request, email);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    @Operation(
        summary = "Get my tasks with pagination",
        description = "Returns paginated list of tasks for logged-in user. " +
                      "Supports sorting by any field."
    )
    public ResponseEntity<Page<TaskResponse>> getMyTasks(
            @AuthenticationPrincipal String email,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            // @Parameter adds description to query parameters in Swagger!

            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Field to sort by", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        return ResponseEntity.ok(
                taskService.getMyTasks(email, page, size, sortBy));
    }

    @GetMapping("/status/{status}")
    @Operation(
        summary = "Filter tasks by status",
        description = "Returns paginated tasks filtered by status. " +
                      "Status values: TODO, IN_PROGRESS, COMPLETED"
    )
    public ResponseEntity<Page<TaskResponse>> getByStatus(
            @Parameter(description = "Task status",example = "TODO")
            @AuthenticationPrincipal String email,
            @PathVariable Task.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
            taskService.getTasksByStatus(
                email, status, page, size));
    }

    @GetMapping("/priority/{priority}")
     @Operation(
        summary = "Filter tasks by priority",
        description = "Returns paginated tasks filtered by priority. " +
                      "Priority values: LOW, MEDIUM, HIGH, URGENT"
    )
    public ResponseEntity<Page<TaskResponse>> getByPriority(
            @Parameter(description = "Task priority",example = "HIGH")
            @AuthenticationPrincipal String email,
            @PathVariable Task.Priority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
            taskService.getTasksByPriority(
                email, priority, page, size));
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search tasks by keyword",
        description = "Searches task titles containing the keyword. " +
                      "Case insensitive search."
    )
    public ResponseEntity<List<TaskResponse>> search(
            @Parameter(description = "Search keyword",example = "bug")
            @AuthenticationPrincipal String email,
            @RequestParam String keyword) {
        return ResponseEntity.ok(
            taskService.searchTasks(email, keyword));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get task by ID",
        description = "Returns a specific task. " +
                      "User can only access their own tasks."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
                     description = "Task found"),
        @ApiResponse(responseCode = "404",
                     description = "Task not found"),
        @ApiResponse(responseCode = "403",
                     description = "Access denied — not your task")
    })
    public ResponseEntity<TaskResponse> getById(
            @Parameter(description = "Task ID", example = "1")
            @PathVariable Long id,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(
            taskService.getTaskById(id, email));
    }

    @PutMapping("/{id}")
     @Operation(
        summary = "Update a task",
        description = "Updates task fields. Only task owner can update."
    )
    public ResponseEntity<TaskResponse> updateTask(
            @Parameter(description = "Task ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(
            taskService.updateTask(id, request, email));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete a task",
        description = "Permanently deletes a task. Only task owner can delete."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
                     description = "Task deleted successfully"),
        @ApiResponse(responseCode = "403",
                     description = "Cannot delete someone else's task"),
        @ApiResponse(responseCode = "404",
                     description = "Task not found")
    })
    public ResponseEntity<String> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal String email) {
        taskService.deleteTask(id, email);
        return ResponseEntity.ok("Task deleted successfully");
    }

    @GetMapping("/overdue")
    @Operation(
        summary = "Get overdue tasks",
        description = "Returns all tasks past their due date " +
                      "that are not yet completed."
    )
    public ResponseEntity<List<TaskResponse>> getOverdue(
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(
            taskService.getOverdueTasks(email));
    }

    @GetMapping("/stats")
    @Operation(
        summary = "Get task statistics",
        description = "Returns count of tasks by status and overdue count. " +
                      "Useful for dashboard display."
    )
    public ResponseEntity<Map<String, Long>> getStats(
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(
            taskService.getTaskStats(email));
    }
}
