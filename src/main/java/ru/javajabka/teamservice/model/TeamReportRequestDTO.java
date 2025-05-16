package ru.javajabka.teamservice.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class TeamReportRequestDTO {

    private final Long teamId;
    private final LocalDate startDate;
    private final LocalDate endDate;
}