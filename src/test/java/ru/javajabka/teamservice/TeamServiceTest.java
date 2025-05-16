package ru.javajabka.teamservice;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.javajabka.teamservice.exception.BadRequestException;
import ru.javajabka.teamservice.model.Role;
import ru.javajabka.teamservice.model.Task;
import ru.javajabka.teamservice.model.TaskStatus;
import ru.javajabka.teamservice.model.Team;
import ru.javajabka.teamservice.model.TeamChangeDTO;
import ru.javajabka.teamservice.model.TeamRequestDTO;
import ru.javajabka.teamservice.model.User;
import ru.javajabka.teamservice.repository.TeamRepository;
import ru.javajabka.teamservice.service.TaskService;
import ru.javajabka.teamservice.service.TeamService;
import ru.javajabka.teamservice.service.UserService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
public class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TaskService taskService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TeamService teamService;

    @Test
    public void createTeam_Valid() {
        Set<Long> teamMembers = new HashSet<>(Arrays.asList(1L, 2L, 3L));
        TeamRequestDTO teamRequestDTO = buildTeamRequestDTO("Team1", 5L, teamMembers);
        Mockito.when(userService.checkAndGetUsers(List.of(1L, 2L, 3L, 5L))).thenReturn(buildUsers());
        Mockito.when(teamRepository.insert(teamRequestDTO)).thenReturn(buildTeam(1L,"Team1", 5L, teamMembers));
        Team createdTeam = teamService.create(teamRequestDTO);
        Assertions.assertEquals(createdTeam, buildTeam(1L,"Team1", 5L, teamMembers));
    }

    @Test
    public void errorCreateTeam() {
        Set<Long> teamMembers = new HashSet<>(Arrays.asList(1L, 2L, 3L));
        TeamRequestDTO teamRequestDTO = buildTeamRequestDTO(null, 5L, teamMembers);
        final BadRequestException teamCreateException = Assertions.assertThrows(
                BadRequestException.class,
                () -> teamService.create(teamRequestDTO)
        );
        Assertions.assertEquals("Введите название для команды", teamCreateException.getMessage());
    }

    @Test
    public void addMemberToTeam_Valid() {
        Set<Long> teamMembers = new HashSet<>(Arrays.asList(1L, 2L, 3L));
        TeamChangeDTO teamChangeDTO = buildTeamChangeDTO(1L, 5L, teamMembers);
        Mockito.when(userService.checkAndGetUsers(List.of(1L, 2L, 3L, 5L))).thenReturn(buildUsers());
        Mockito.when(teamRepository.addMember(teamChangeDTO)).thenReturn(buildTeam(1L, "Team1",5L, teamMembers));
        Team createdTeam = teamService.addMember(teamChangeDTO);
        Assertions.assertEquals(createdTeam, buildTeam(1L, "Team1", 5L, teamMembers));
    }

    @Test
    public void errorAddMemberToTeam() {
        Set<Long> teamMembers = new HashSet<>(Arrays.asList(1L, 2L, 3L));
        TeamChangeDTO teamChangeDTO = buildTeamChangeDTO(null, 5L, teamMembers);
        final BadRequestException teamCreateException = Assertions.assertThrows(
                BadRequestException.class,
                () -> teamService.addMember(teamChangeDTO)
        );
        Assertions.assertEquals("Введите id команды больше нуля", teamCreateException.getMessage());
    }

    @Test
    public void removeMemberToTeam_Valid() {
        Set<Long> teamMembersAfterRemove = new HashSet<>(Arrays.asList(3L));
        Set<Long> teamMembers = new HashSet<>(Arrays.asList(1L, 2L));
        TeamChangeDTO teamChangeDTO = buildTeamChangeDTO(1L, 5L, teamMembers);
        Mockito.when(userService.checkAndGetUsers(List.of(1L, 2L, 5L))).thenReturn(buildUsers());
        Mockito.when(teamRepository.removeMember(teamChangeDTO)).thenReturn(buildTeam(1L, "Team1",5L, teamMembersAfterRemove));
        Team createdTeam = teamService.removeMember(teamChangeDTO);
        Assertions.assertEquals(createdTeam, buildTeam(1L, "Team1", 5L, teamMembersAfterRemove));
    }

    @Test
    public void errorRemoveMemberToTeam() {
        Set<Long> teamMembersAfterRemove = new HashSet<>(Arrays.asList(3L));
        TeamChangeDTO teamChangeDTO = buildTeamChangeDTO(null, 5L, teamMembersAfterRemove);
        final BadRequestException teamCreateException = Assertions.assertThrows(
                BadRequestException.class,
                () -> teamService.removeMember(teamChangeDTO)
        );
        Assertions.assertEquals("Введите id команды больше нуля", teamCreateException.getMessage());
    }

    @Test
    public void getTeamTasks_Valid() {
        Mockito.when(teamRepository.getTeamMembersByid(1L)).thenReturn(List.of(1L, 2L, 3L));
        Mockito.when(taskService.getUserTasks(List.of(1L, 2L, 3L))).thenReturn(buildTasks());
        List<Task> foundTasks = teamService.getTeamTasks(1L);
        Assertions.assertEquals(foundTasks, buildTasks());
    }

    private TeamRequestDTO buildTeamRequestDTO(String name, Long managerId, Set<Long> teamMemberIds) {
        return TeamRequestDTO.builder()
                .name(name)
                .managerId(managerId)
                .memberIds(teamMemberIds)
                .build();
    }

    private TeamChangeDTO buildTeamChangeDTO(Long teamId, Long managerId, Set<Long> teamMemberIds) {
        return TeamChangeDTO.builder()
                .team(teamId)
                .manager(managerId)
                .members(teamMemberIds)
                .build();
    }

    private Team buildTeam(Long id, String name, Long managerId, Set<Long> memberIds) {
        return Team.builder()
                .id(1L)
                .name("Team1")
                .managerId(5L)
                .members(memberIds)
                .build();
    }

    private List<Task> buildTasks() {
        List<Task> tasks = new ArrayList<>();

        tasks.add(
                Task.builder()
                        .id(1L)
                        .title("Title1")
                        .description("Description1")
                        .status(TaskStatus.DONE)
                        .deadLine(LocalDate.of(2025, 9, 5))
                        .build()
        );

        tasks.add(
                Task.builder()
                        .id(1L)
                        .title("Title2")
                        .description("Description2")
                        .status(TaskStatus.DONE)
                        .deadLine(LocalDate.of(2025, 9, 7))
                        .build()
        );

        return tasks;
    }

    private List<User> buildUsers() {
        List<User> users = new ArrayList<>();
        users.add(
                User.builder()
                        .id(1L)
                        .userName("User1")
                        .role(Role.USER)
                        .build()
        );

        users.add(
                User.builder()
                        .id(2L)
                        .userName("User2")
                        .role(Role.USER)
                        .build()
        );

        users.add(
                User.builder()
                        .id(3L)
                        .userName("User3")
                        .role(Role.USER)
                        .build()
        );

        users.add(
                User.builder()
                        .id(5L)
                        .userName("User5")
                        .role(Role.MANAGER)
                        .build()
        );

        return users;
    }
}