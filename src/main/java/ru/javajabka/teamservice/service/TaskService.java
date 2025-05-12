package ru.javajabka.teamservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.javajabka.teamservice.model.Task;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final RestTemplate restTemplate;

    @Value("${url.service.task}")
    private String taskServiceUrl;

    public List<Task> getUserTasks(final List<Long> userIds) {
        List<Task> allUsersTasks = new ArrayList<>();

        for (Long userId : userIds) {
            String url = UriComponentsBuilder
                    .fromUriString(taskServiceUrl + "/api/v1/task")
                    .queryParam("assignee", userId)
                    .encode()
                    .build()
                    .toString();

            ResponseEntity<List<Task>> responseEntity =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {
                            }
                    );

            allUsersTasks.addAll(responseEntity.getBody());
        }

        return allUsersTasks;
    }
}