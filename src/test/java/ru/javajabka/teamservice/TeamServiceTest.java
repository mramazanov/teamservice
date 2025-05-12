package ru.javajabka.teamservice;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.javajabka.teamservice.model.Event;
import ru.javajabka.teamservice.model.Task;
import ru.javajabka.teamservice.model.TaskStatus;
import ru.javajabka.teamservice.model.TeamReport;
import ru.javajabka.teamservice.model.TeamReportRequestDTO;
import ru.javajabka.teamservice.model.User;
import ru.javajabka.teamservice.repository.TeamRepository;
import ru.javajabka.teamservice.service.EventService;
import ru.javajabka.teamservice.service.TaskService;
import ru.javajabka.teamservice.service.TeamService;
import ru.javajabka.teamservice.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
public class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TaskService taskService;

    @Mock
    private EventService eventService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TeamService teamService;

    @Test
    public void testCreateTeam() {
        TeamReportRequestDTO teamReportRequestDTO = TeamReportRequestDTO.builder()
                .teamId(1L)
                .startDate(LocalDate.of(2025, 5, 5))
                .endDate(LocalDate.of(2025, 5, 12))
                .build();

        Mockito.when(teamRepository.getTeamMembersById(1L)).thenReturn(List.of(1L, 2L, 3L));
        Mockito.when(userService.checkAndGetUsers(List.of(1L, 2L, 3L))).thenReturn(getUsers());
        Mockito.when(taskService.getUserTasks(List.of(1L, 2L, 3L))).thenReturn(getTasks());
        Mockito.when(eventService.getEventsTasks(getTasks().stream().map(Task::getId).collect(Collectors.toSet()))).thenReturn(getEvents());

        TeamReport report = buildTeamReportObj();

        TeamReport teamReport = teamService.getTeamReport(teamReportRequestDTO);
        Assertions.assertEquals(report, teamReport);
        Mockito.verify(teamRepository).getTeamMembersById(1L);
        Mockito.verify(userService).checkAndGetUsers(List.of(1L, 2L, 3L));
        Mockito.verify(taskService).getUserTasks(List.of(1L, 2L, 3L));
        Mockito.verify(eventService).getEventsTasks(getTasks().stream().map(Task::getId).collect(Collectors.toSet()));
    }

    private TeamReport buildTeamReportObj() {
        Map<String, Long> taskPerStatus = new HashMap<>();
        taskPerStatus.put("DONE", 6L);

        Map<String, Long> mostActiveUsers = new HashMap<>();
        mostActiveUsers.put("user1", 3L);

        return TeamReport.builder()
                .teamTasks(6)
                .tasksPerStatus(taskPerStatus)
                .mostActiveUser(mostActiveUsers)
                .averageTaskCompleteTime(160L)
                .build();
    }

    private List<User> getUsers() {
        List<User> users = new ArrayList<>();
        users.add(User.builder().id(1L).userName("user1").build());
        users.add(User.builder().id(2L).userName("user2").build());
        users.add(User.builder().id(3L).userName("user3").build());
        return users;
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();

        tasks.add(
                Task.builder()
                        .id(1L)
                        .assignee(1L)
                        .status(TaskStatus.DONE)
                        .createdAt(LocalDateTime.of(2025, 5, 6, 14, 50))
                        .build()
        );

        tasks.add(
                Task.builder()
                        .id(2L)
                        .assignee(1L)
                        .status(TaskStatus.DONE)
                        .createdAt(LocalDateTime.of(2025, 5, 6, 15, 10))
                        .build()
        );

        tasks.add(
                Task.builder()
                        .id(3L)
                        .assignee(1L)
                        .status(TaskStatus.DONE)
                        .createdAt(LocalDateTime.of(2025, 5, 6, 15, 15))
                        .build()
        );

        tasks.add(
                Task.builder()
                        .id(4L)
                        .assignee(2L)
                        .status(TaskStatus.DONE)
                        .createdAt(LocalDateTime.of(2025, 5, 6, 14, 10))
                        .build()
        );

        tasks.add(
                Task.builder()
                        .id(5L)
                        .assignee(3L)
                        .status(TaskStatus.DONE)
                        .createdAt(LocalDateTime.of(2025, 5, 6, 15, 20))
                        .build()
        );

        tasks.add(
                Task.builder()
                        .id(6L)
                        .assignee(3L)
                        .status(TaskStatus.DONE)
                        .createdAt(LocalDateTime.of(2025, 5, 6, 14, 55)).build()
        );

        return tasks;
    }

    private List<Event> getEvents() {
        List<Event> events = new ArrayList<>();

        events.add(
                Event.builder()
                        .id(1L)
                        .eventName("task_created")
                        .to("")
                        .taskId(1L)
                        .eventDateTime(LocalDateTime.of(2025, 5, 6, 14, 50))
                        .build()
        );

        events.add(
                Event.builder()
                        .id(2L)
                        .eventName("status_changed")
                        .taskId(1L)
                        .from("IN_PROGRESS")
                        .to("DONE")
                        .eventDateTime(LocalDateTime.of(2025, 5, 6, 17, 50))
                        .build()
        );

        events.add(
                Event.builder()
                        .id(3L)
                        .eventName("task_created")
                        .to("")
                        .taskId(2L)
                        .eventDateTime(LocalDateTime.of(2025, 5, 6, 15, 10))
                        .build()
        );

        events.add(
                Event.builder()
                        .id(4L)
                        .eventName("status_changed")
                        .taskId(2L)
                        .from("IN_PROGRESS")
                        .to("DONE")
                        .eventDateTime(LocalDateTime.of(2025, 5, 6, 17, 30))
                        .build()
        );

        events.add(
                Event.builder()
                        .id(5L)
                        .eventName("task_created")
                        .to("")
                        .taskId(3L)
                        .eventDateTime(LocalDateTime.of(2025, 5, 6, 15, 15))
                        .build()
        );

        events.add(
                Event.builder()
                        .id(6L)
                        .eventName("status_changed")
                        .taskId(3L)
                        .from("IN_PROGRESS")
                        .to("DONE")
                        .eventDateTime(LocalDateTime.of(2025, 5, 6, 17, 55))
                        .build()
        );

        return events;
    }
}