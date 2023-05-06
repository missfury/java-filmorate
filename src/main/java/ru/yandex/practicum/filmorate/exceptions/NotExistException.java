package ru.yandex.practicum.filmorate.exceptions;

public class NotExistException extends RuntimeException {
    public NotExistException(String message) {
        super(message);
    }
}