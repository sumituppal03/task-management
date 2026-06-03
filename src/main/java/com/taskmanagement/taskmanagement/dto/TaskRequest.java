package com.taskmanagement.taskmanagement.dto;

import com.taskmanagement.taskmanagement.model.Task;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
@Schema(description = "Request body for creating or updating a task")
public class TaskRequest {

    @NotBlank(message = "Title is required")
    @Schema(description="Title is required",example="Fix login bug",required=true)
    private String title;

    @Schema(description = "Detailed task description",
            example = "The login page crashes when password is wrong")
    private String description;

    @NotNull(message = "Priority is required")
    @Schema(description = "Task priority level",
            example = "HIGH",
            allowableValues = {"LOW", "MEDIUM", "HIGH", "URGENT"})
    private Task.Priority priority;

    @Schema(description = "Task status",
            example = "TODO",
            allowableValues = {"TODO", "IN_PROGRESS", "COMPLETED"})
    private Task.Status status;

    @FutureOrPresent(message = "Due date cannot be in the past")
    @Schema(description = "Task due date (cannot be in past)",example = "2026-05-15")
    private LocalDate dueDate;
}