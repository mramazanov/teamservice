package ru.javajabka.teamservice.exception;

public class BadRequestException extends RuntimeException{
    public BadRequestException(final String message) {
        super(message);
    }
}