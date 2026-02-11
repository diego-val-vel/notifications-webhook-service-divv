package io.notifications.webhook.domain.model;

/*
 * AttemptType represents the kind of delivery attempt being recorded.
 *
 * This challenge focuses on manual replay attempts triggered via the self-service API.
 */
public enum AttemptType {
    REPLAY
}