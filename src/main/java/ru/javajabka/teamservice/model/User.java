package ru.javajabka.teamservice.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private final Long id;
    private final String userName;
    private final Role role;
}