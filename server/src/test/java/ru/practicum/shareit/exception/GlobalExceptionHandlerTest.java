package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFoundException_shouldReturn404() {
        NotFoundException ex = new NotFoundException("Not found");

        ResponseEntity<Map<String, Object>> response = handler.handleNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Not found", response.getBody().get("error"));
        assertEquals(404, response.getBody().get("status"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleValidationException_shouldReturn400() {
        ValidationException ex = new ValidationException("Validation failed");

        ResponseEntity<Map<String, Object>> response = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().get("error"));
        assertEquals(400, response.getBody().get("status"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleForbiddenException_shouldReturn403() {
        ForbiddenException ex = new ForbiddenException("Forbidden");

        ResponseEntity<Map<String, Object>> response = handler.handleForbiddenException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Forbidden", response.getBody().get("error"));
        assertEquals(403, response.getBody().get("status"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleConflictException_shouldReturn409() {
        ConflictException ex = new ConflictException("Conflict");

        ResponseEntity<Map<String, Object>> response = handler.handleConflictException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Conflict", response.getBody().get("error"));
        assertEquals(409, response.getBody().get("status"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleBadRequestException_shouldReturn400() {
        BadRequestException ex = new BadRequestException("Bad request");

        ResponseEntity<Map<String, Object>> response = handler.handleBadRequestException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Bad request", response.getBody().get("error"));
        assertEquals(400, response.getBody().get("status"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleGenericException_shouldReturn500() {
        RuntimeException ex = new RuntimeException("Something went wrong");

        ResponseEntity<Map<String, Object>> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Something went wrong", response.getBody().get("error"));
        assertEquals(500, response.getBody().get("status"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleMethodArgumentNotValid_shouldReturn400AndFirstValidationMessage() {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "object");

        bindingResult.addError(new ObjectError("object", "Ошибка валидации DTO"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleMethodArgumentNotValid(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Ошибка валидации DTO", response.getBody().get("error"));
        assertEquals(400, response.getBody().get("status"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleMethodArgumentNotValid_shouldReturnFirstErrorMessage_whenMultipleErrorsExist() {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "object");

        bindingResult.addError(new ObjectError("object", "Первая ошибка"));
        bindingResult.addError(new ObjectError("object", "Вторая ошибка"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleMethodArgumentNotValid(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Первая ошибка", response.getBody().get("error"));
        assertEquals(400, response.getBody().get("status"));
        assertNotNull(response.getBody().get("timestamp"));
    }
}