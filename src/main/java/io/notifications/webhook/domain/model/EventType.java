package io.notifications.webhook.domain.model;

import java.util.Arrays;

/*
 * EventType represents the business classification of a notification event.
 * It defines the supported event types and provides a controlled mapping
 * from external string representations into the domain.
 */
public enum EventType {

    CREDIT_CARD_PAYMENT("credit_card_payment"),
    DEBIT_CARD_WITHDRAWAL("debit_card_withdrawal"),
    CREDIT_TRANSFER("credit_transfer"),
    DEBIT_AUTOMATIC_PAYMENT("debit_automatic_payment"),
    CREDIT_REFUND("credit_refund"),
    DEBIT_TRANSFER("debit_transfer"),
    CREDIT_DEPOSIT("credit_deposit"),
    DEBIT_PURCHASE("debit_purchase"),
    CREDIT_CASHBACK("credit_cashback"),
    DEBIT_SUBSCRIPTION("debit_subscription");

    private final String externalValue;

    EventType(String externalValue) {
        this.externalValue = externalValue;
    }

    public String externalValue() {
        return externalValue;
    }

    public static EventType fromExternalValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("EventType value must not be null or blank");
        }

        return Arrays.stream(values())
                .filter(type -> type.externalValue.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported EventType: " + value));
    }
}