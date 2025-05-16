package ru.javajabka.teamservice.repository;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamMember {
    private final Long id;
    private final Long memberId;
}
