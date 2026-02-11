package io.notifications.webhook.adapters.in.rest;

import io.notifications.webhook.domain.model.NotificationEventNotFound;
import io.notifications.webhook.domain.model.ReplayNotAllowed;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/*
 * RestExceptionHandler centralizes the translation of domain and validation exceptions
 * into RFC 7807 Problem Details responses.
 *
 * It ensures a consistent error contract across the API surface while keeping
 * controllers focused exclusively on request handling and use case orchestration.
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
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ProblemDetail handleReplayNotAllowed(ReplayNotAllowed ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
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

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleUnexpected(Exception ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Unexpected error");
        pd.setDetail("An unexpected error occurred");
        pd.setProperty("exception", ex.getClass().getName());
        return pd;
    }
}