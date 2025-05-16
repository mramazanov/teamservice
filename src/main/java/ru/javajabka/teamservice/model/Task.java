package ru.javajabka.teamservice.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class Task implements Comparable<Task> {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDate deadLine;
    private Long author;
    private Long assignee;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Override
    public int compareTo(Task t) {
        return Integer.parseInt(String.valueOf(this.getAssignee() - t.getAssignee()));
    }
}