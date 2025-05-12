package ru.javajabka.teamservice.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Event {

    private final Long id;
    private final String eventName;
    private final Long taskId;
    private final String from;
    private final String to;
    private final LocalDateTime eventDateTime;
}