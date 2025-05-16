package ru.javajabka.teamservice.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class TeamReport {

    private final Integer teamTasks;
    private final Map<String, Long> tasksPerStatus;
    private final Map<String, Long> mostActiveUser;
    private final Long averageTaskCompleteTime;
}