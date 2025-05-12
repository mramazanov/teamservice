package ru.javajabka.teamservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.javajabka.teamservice.model.Event;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EventService {
    private final RestTemplate restTemplate;

    @Value("${url.service.event}")
    private String eventServiceUrl;

    public List<Event> getEventsTasks(final Set<Long> taskIds) {

        String url = UriComponentsBuilder
                .fromUriString(eventServiceUrl + "/api/v1/event")
                .queryParam("ids", taskIds)
                .encode()
                .build()
                .toString();

        ResponseEntity<List<Event>> responseEntity =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {
                        }
                );

        return responseEntity.getBody();
    }
}