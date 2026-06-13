package com.taskmanagement.taskmanagement.dto;

import java.io.Serializable;
import com.taskmanagement.taskmanagement.model.Task;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TaskResponse implements Serializable{

    private Long id;
    private String title;
    private String description;
    private String priority;
    private String status;
    private LocalDate dueDate;
    private String ownerName;
    private String ownerEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean overdue;
    private static final long serialVersionUID = 1L;
}
