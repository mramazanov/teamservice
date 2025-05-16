package ru.javajabka.teamservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.javajabka.teamservice.exception.BadRequestException;
import ru.javajabka.teamservice.model.Role;
import ru.javajabka.teamservice.model.Task;
import ru.javajabka.teamservice.model.Team;
import ru.javajabka.teamservice.model.TeamChangeDTO;
import ru.javajabka.teamservice.model.TeamRequestDTO;
import ru.javajabka.teamservice.model.User;
import ru.javajabka.teamservice.repository.TeamRepository;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserService userService;
    private final TaskService taskService;

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
    public List<Task> getTeamTasks(final Long teamId) {
        List<Long> members = teamRepository.getTeamMembersByid(teamId);
        return taskService.getUserTasks(members);
    }

    @Transactional(readOnly = true)
    public List<Long> getMembers(Long id) {
        List<Long> teamMembers = teamRepository.getTeamMembersByid(id);
        if (teamMembers.isEmpty()) {
            throw new BadRequestException(String.format("Участники команды с id %d не найдены", id));
        }
        return teamMembers;
    }

    private void validate(final TeamRequestDTO teamRequestDTO) {
        if (teamRequestDTO == null) {
            throw new BadRequestException("Введите данные для создания команды");
        }

        if (teamRequestDTO.getManagerId() == null || teamRequestDTO.getManagerId() <= 0) {
            throw new BadRequestException("Введите id менеджера больше нуля");
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