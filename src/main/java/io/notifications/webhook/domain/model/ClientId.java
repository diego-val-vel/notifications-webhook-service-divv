package io.notifications.webhook.domain.model;

import java.util.Objects;

/*
 * ClientId is a domain value object that represents the unique identifier of a client.
 * It prevents the use of raw strings across the domain and enforces basic invariants.
 */
public final class ClientId {

    private final String value;

    private ClientId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ClientId value must not be null or blank");
        }
        this.value = value;
    }

    public static ClientId of(String value) {
        return new ClientId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientId clientId)) return false;
        return Objects.equals(value, clientId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}