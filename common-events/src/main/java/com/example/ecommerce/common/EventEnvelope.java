package com.example.ecommerce.common;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope<T>(
        String eventId,
        String eventType,
        String correlationId,
        Instant occurredAt,
        T payload
) {
    public static <T> EventEnvelope<T> of(String eventType, String correlationId, T payload) {
        return new EventEnvelope<>(UUID.randomUUID().toString(), eventType, correlationId, Instant.now(), payload);
    }
}
