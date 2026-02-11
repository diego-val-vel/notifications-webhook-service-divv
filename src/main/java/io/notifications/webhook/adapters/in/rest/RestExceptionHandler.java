package io.notifications.webhook.adapters.in.rest;

import io.notifications.webhook.domain.model.NotificationEventNotFound;
import io.notifications.webhook.domain.model.ReplayNotAllowed;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/*
 * RestExceptionHandler centralizes the translation of domain, validation, and framework exceptions
 * into RFC 7807 Problem Details responses.
 *
 * It ensures a consistent error contract across the API surface while keeping controllers focused
 * exclusively on request handling and use case orchestration.
 */
@RestControllerAdvice
public final class RestExceptionHandler {

    @ExceptionHandler(NotificationEventNotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleNotFound(NotificationEventNotFound ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("Notification event not found");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(ReplayNotAllowed.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleReplayNotAllowed(ReplayNotAllowed ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Replay not allowed");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleBadRequest(IllegalArgumentException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Invalid request");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Invalid request parameter");
        pd.setDetail(buildTypeMismatchMessage(ex));
        return pd;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation failed");
        pd.setDetail(buildConstraintViolationMessage(ex));
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation failed");
        pd.setDetail(buildMethodArgumentNotValidMessage(ex));
        return pd;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleUnexpected(Exception ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Unexpected error");
        pd.setDetail("An unexpected error occurred");
        pd.setProperty("exception", ex.getClass().getName());
        return pd;
    }

    private static String buildTypeMismatchMessage(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName();
        Object value = ex.getValue();
        Class<?> requiredType = ex.getRequiredType();

        String valueText = value == null ? "null" : String.valueOf(value);
        String typeText = requiredType == null ? "unknown" : requiredType.getSimpleName();

        return "Invalid value for parameter '" + name + "': '" + valueText + "'. Expected type: " + typeText + ".";
    }

    private static String buildConstraintViolationMessage(ConstraintViolationException ex) {
        if (ex.getConstraintViolations() == null || ex.getConstraintViolations().isEmpty()) {
            return "Constraint violation";
        }

        return ex.getConstraintViolations()
                .stream()
                .map(RestExceptionHandler::formatViolation)
                .collect(Collectors.joining("; "));
    }

    private static String formatViolation(ConstraintViolation<?> v) {
        String path = v.getPropertyPath() == null ? "" : v.getPropertyPath().toString();
        String message = v.getMessage() == null ? "invalid value" : v.getMessage();
        if (path.isBlank()) {
            return message;
        }
        return path + ": " + message;
    }

    private static String buildMethodArgumentNotValidMessage(MethodArgumentNotValidException ex) {
        if (ex.getBindingResult() == null || ex.getBindingResult().getFieldErrors() == null) {
            return "Validation error";
        }

        return ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
    }
}