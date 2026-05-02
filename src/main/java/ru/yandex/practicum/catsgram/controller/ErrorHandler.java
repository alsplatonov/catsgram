package ru.yandex.practicum.catsgram.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.catsgram.exception.*;

@RestControllerAdvice
public class ErrorHandler {

    // 404
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFoundException e) {
        return new ErrorResponse(e.getMessage());
    }

    // 409
    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicate(DuplicatedDataException e) {
        return new ErrorResponse(e.getMessage());
    }

    // 422
    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse handleConditions(ConditionsNotMetException e) {
        return new ErrorResponse(e.getMessage());
    }

    // 400
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleParameter(ParameterNotValidException e) {
        String message = "Некорректное значение параметра " +
                e.getParameter() + ": " + e.getReason();
        return new ErrorResponse(message);
    }

    // 500 — всё остальное
    /*
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleOther(Throwable e) {
        return new ErrorResponse("Произошла непредвиденная ошибка.");
    }
    */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleOther(Throwable e) {
        e.printStackTrace(); // 👈 ВАЖНО
        return new ErrorResponse("Произошла непредвиденная ошибка.");
    }
}