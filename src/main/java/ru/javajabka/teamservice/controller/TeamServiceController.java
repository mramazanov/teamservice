package ru.javajabka.teamservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.javajabka.teamservice.model.Team;
import ru.javajabka.teamservice.model.TeamChangeDTO;
import ru.javajabka.teamservice.model.TeamReport;
import ru.javajabka.teamservice.model.TeamReportRequestDTO;
import ru.javajabka.teamservice.model.TeamRequestDTO;
import ru.javajabka.teamservice.service.TeamService;

@RestController
@RequestMapping("/api/v1/team")
@RequiredArgsConstructor
@Tag(name = "Команда")
public class TeamServiceController {

    private final TeamService teamService;

    @PostMapping
    @Operation(summary = "Создать команду")
    public Team createTeam(@RequestBody final TeamRequestDTO teamRequestDTO) {
        return teamService.create(teamRequestDTO);
    }

    @PatchMapping
    @Operation(summary = "Добавить участника")
    public Team addMember(@RequestBody final TeamChangeDTO teamChangeDTO) {
        return teamService.addMember(teamChangeDTO);
    }

    @DeleteMapping
    @Operation(summary = "Удалить участника")
    public Team removeMember(@RequestBody final TeamChangeDTO teamChangeDTO) {
        return teamService.removeMember(teamChangeDTO);
    }

    @GetMapping("/report")
    @Operation(summary = "Получить задачи команды")
    public TeamReport getTeamReport(final TeamReportRequestDTO teamReportRequestDTO) {
        return teamService.getTeamReport(teamReportRequestDTO);
    }
}