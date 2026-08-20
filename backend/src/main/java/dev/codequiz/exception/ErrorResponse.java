package dev.codequiz.exception;

import java.time.LocalDateTime;

// Единый формат тела ответа для любой ошибки API — чтобы фронтенд всегда
// получал предсказуемую структуру, а не то HTML-страницу Tomcat, то JSON.
public class ErrorResponse {

    private int status;
    private String message;
    private LocalDateTime timestamp;

    public ErrorResponse() {
    }

    public ErrorResponse(int status, String message, LocalDateTime timestamp) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}