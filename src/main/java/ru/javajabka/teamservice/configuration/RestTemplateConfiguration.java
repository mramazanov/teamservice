package ru.javajabka.teamservice.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.javajabka.teamservice.exception.RestTemplateResponseErrorHandler;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    public RestTemplate restTemplate(RestTemplateResponseErrorHandler errorHandler) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(errorHandler);
        return restTemplate;
    }
}