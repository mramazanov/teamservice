package ru.javajabka.teamservice.model;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class TeamChangeDTO {

    private final Long team;
    private final Long manager;
    private final Set<Long> members;
}