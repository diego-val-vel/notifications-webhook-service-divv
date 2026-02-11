package io.notifications.webhook.domain.model;

/*
 * DeliveryAttemptResult represents the final outcome of a webhook delivery attempt.
 *
 * SUCCESS is recorded when the webhook target returns a 2xx HTTP status.
 * FAILURE is recorded for non-2xx responses, timeouts, or any exception during delivery.
 */
public enum DeliveryAttemptResult {
    SUCCESS,
    FAILURE
}