package ru.javajabka.teamservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.javajabka.teamservice.repository.TeamRepository;
import ru.javajabka.teamservice.service.TaskService;
import ru.javajabka.teamservice.service.TeamService;
import ru.javajabka.teamservice.service.UserService;

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
    public void testCreateTeam() {

    }

}