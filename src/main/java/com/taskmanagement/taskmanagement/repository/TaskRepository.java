package com.taskmanagement.taskmanagement.repository;
import com.taskmanagement.taskmanagement.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
public interface TaskRepository extends JpaRepository<Task,Long>{
    Page<Task> findByUserId(Long userId, Pageable pageable);

    Page<Task> findByUserIdAndStatus(Long userId, Task.Status status, Pageable pageable);

    Page<Task> findByUserIdAndPriority(Long userId, Task.Priority priority, Pageable pageable);

    List<Task> findByUserIdAndTitleContainingIgnoreCase(Long userId, String keyword);
    List<Task> findByUserIdAndDueDateBeforeAndStatusNot(Long userId, LocalDate date, Task.Status status);

    @Query("SELECT t FROM Task t WHERE t.user.id = :userId " +"AND t.status != 'COMPLETED' " +"AND t.dueDate < :today")
    List<Task> findOverdueTasks(@Param("userId") Long userId,@Param("today") LocalDate today);
    Long countByUserIdAndStatus(Long userId, Task.Status status);        
}
