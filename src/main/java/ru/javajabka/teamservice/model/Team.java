package ru.javajabka.teamservice.model;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Builder
@Data
public class Team {

    private final Long id;
    private final String name;
    private final Long managerId;
    private final Set<Long> members;
}