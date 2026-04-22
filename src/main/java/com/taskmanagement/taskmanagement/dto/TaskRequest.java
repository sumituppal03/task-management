package com.taskmanagement.taskmanagement.dto;

import com.taskmanagement.taskmanagement.model.Task;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Priority is required")
    private Task.Priority priority;

    private Task.Status status;

    @FutureOrPresent(message = "Due date cannot be in the past")
    private LocalDate dueDate;
}