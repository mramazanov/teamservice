package ru.javajabka.teamservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.javajabka.teamservice.exception.BadRequestException;
import ru.javajabka.teamservice.model.Event;
import ru.javajabka.teamservice.model.Role;
import ru.javajabka.teamservice.model.Task;
import ru.javajabka.teamservice.model.Team;
import ru.javajabka.teamservice.model.TeamChangeDTO;
import ru.javajabka.teamservice.model.TeamReport;
import ru.javajabka.teamservice.model.TeamReportRequestDTO;
import ru.javajabka.teamservice.model.TeamRequestDTO;
import ru.javajabka.teamservice.model.User;
import ru.javajabka.teamservice.repository.TeamRepository;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserService userService;
    private final TaskService taskService;
    private final EventService eventService;

    @Transactional(rollbackFor = Exception.class)
    public Team create(final TeamRequestDTO teamRequestDTO) {
        validate(teamRequestDTO);
        return teamRepository.insert(teamRequestDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    public Team addMember(final TeamChangeDTO teamChangeDTO) {
        validate(teamChangeDTO);
        return teamRepository.addMember(teamChangeDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    public Team removeMember(final TeamChangeDTO teamChangeDTO) {
        validate(teamChangeDTO);
        return teamRepository.removeMember(teamChangeDTO);
    }

    @Transactional(readOnly = true)
    public TeamReport getTeamReport(final TeamReportRequestDTO teamReportRequestDTO) {
        List<Long> membersOfTeam = teamRepository.getTeamMembersById(teamReportRequestDTO.getTeamId());
        List<User> checkedUsers = userService.checkAndGetUsers(membersOfTeam);
        List<Task> tasks = taskService.getUserTasks(membersOfTeam);

        tasks = tasks.stream()
                .filter(t -> t.getCreatedAt().isAfter(teamReportRequestDTO.getStartDate().atStartOfDay())
                                && t.getCreatedAt().isBefore(teamReportRequestDTO.getEndDate().atStartOfDay()))
                .toList();

        Map<String, Long> teamTasksWithStatus = getAllTasksWithStatus(tasks);
        Map<String, Long> tasksPerUser = getTasksPerUser(checkedUsers, tasks);
        Long avgTimeTask = getTeamTasksDuration(tasks, teamReportRequestDTO);

        return TeamReport.builder()
                .teamTasks(tasks.size())
                .tasksPerStatus(teamTasksWithStatus)
                .mostActiveUser(tasksPerUser)
                .averageTaskCompleteTime(avgTimeTask)
                .build();
    }

    private Map<String, Long> getAllTasksWithStatus(List<Task> tasks) {
        Map<String, Long> allTasksWithStatus = new HashMap<>();
        long TO_DO_COUNT = 0L;
        long IN_PROGRESS_COUNT = 0L;
        long DONE = 0L;

        for (Task task : tasks) {
            switch (task.getStatus()) {
                case TO_DO:
                    allTasksWithStatus.put(task.getStatus().toString(), ++TO_DO_COUNT);
                    break;
                case IN_PROGRESS:
                    allTasksWithStatus.put(task.getStatus().toString(), ++IN_PROGRESS_COUNT);
                    break;
                case DONE:
                    allTasksWithStatus.put(task.getStatus().toString(), ++DONE);
                    break;
            }
        }

        return allTasksWithStatus;
    }

    private Long getTeamTasksDuration(List<Task> tasks, TeamReportRequestDTO teamReportRequestDTO) {
        List<Event> taskEvents = eventService.getEventsTasks(tasks.stream().map(Task::getId).collect(Collectors.toSet()));
        final Map<Long, Long> taskStatistics = new HashMap<>();

        for (Task task : tasks) {
            taskEvents.stream()
                    .filter(taskEvent -> taskEvent.getTaskId().equals(task.getId())
                            && taskEvent.getEventName().equals("status_changed")
                            && taskEvent.getTo().equals("DONE") && taskEvent.getEventDateTime().isBefore(teamReportRequestDTO.getEndDate().atStartOfDay()))
                    .findFirst()
                    .ifPresent(taskFinishedEvent -> {
                        LocalDateTime createdDateTime = taskEvents.stream()
                                .filter(t -> t.getTaskId().equals(taskFinishedEvent.getTaskId())
                                                && t.getEventName().equals("task_created"))
                                .findFirst()
                                .get()
                                .getEventDateTime();

                                Instant created = createdDateTime.toInstant(ZoneOffset.UTC);
                                Instant finished = taskFinishedEvent.getEventDateTime().toInstant(ZoneOffset.UTC);
                                long durationTask = Duration.between(created, finished).toMinutes();
                                taskStatistics.put(taskFinishedEvent.getTaskId(), durationTask);
                    });
        }

        Optional<Long> avgTime =  taskStatistics.values().stream().reduce(Long::sum);
        if (avgTime.isEmpty()) {
            return null;
        }
        return avgTime.get() / taskStatistics.size();
    }

    private Map<String, Long> getTasksPerUser(List<User> users, List<Task> tasks) {
        Map<String, Long> tasksPerUser = new HashMap<>();

        for (User u : users) {
            AtomicLong userTaskCount = new AtomicLong();
            for (Task t : tasks) {
                if (t.getAssignee().equals(u.getId())) {
                    userTaskCount.getAndIncrement();
                }
                tasksPerUser.put(u.getUserName(), userTaskCount.get());
            }
        }

        Long maxTaskCountUser = tasksPerUser.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .toList()
                .getLast()
                .getValue();

        return tasksPerUser.entrySet().stream()
                .filter(t -> t.getValue().equals(maxTaskCountUser))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void validate(final TeamRequestDTO teamRequestDTO) {
        if (teamRequestDTO == null) {
            throw new BadRequestException("Введите данные для создания команды");
        }

        if (teamRequestDTO.getManagerId() == null || teamRequestDTO.getManagerId() <= 0) {
            throw new BadRequestException("Введите id менедера больше нуля");
        }

        if (teamRequestDTO.getMemberIds() == null || teamRequestDTO.getMemberIds().size() <= 1) {
            throw new BadRequestException("Количество участников в команде должно быть больше 1");
        }

        if (teamRequestDTO.getName() == null || teamRequestDTO.getName().isEmpty()) {
            throw new BadRequestException("Введите название для команды");
        }

        collectAndCheckUsers(teamRequestDTO.getManagerId(), teamRequestDTO.getMemberIds());
    }

    private void validate(TeamChangeDTO teamChangeDTO) {
        if (teamChangeDTO.getTeam() == null || teamChangeDTO.getTeam() < 1) {
            throw new BadRequestException("Введите id команды больше нуля");
        }
        if (teamChangeDTO.getMembers() == null || teamChangeDTO.getMembers().isEmpty()) {
            throw new BadRequestException("Количество добавляемых участников должно быть больше нуля");
        }

        collectAndCheckUsers(teamChangeDTO.getManager(), teamChangeDTO.getMembers());
    }

    private void checkUserRole(List<User> users, Long editorId) {
        users.stream().filter(user -> user.getId().equals(editorId)).findFirst().ifPresent(user -> {
            if (!user.getRole().equals(Role.MANAGER)) {
                throw new BadRequestException(String.format("Пользователь с id %d не является менеджером", user.getId()));
            }
        });
    }

    private void collectAndCheckUsers(Long managerId, Set<Long> memberIds) {
        memberIds.add(managerId);
        List<User> checkedUsers = userService.checkAndGetUsers(memberIds.stream().toList());
        checkUserRole(checkedUsers, managerId);
        memberIds.remove(managerId);
    }
}