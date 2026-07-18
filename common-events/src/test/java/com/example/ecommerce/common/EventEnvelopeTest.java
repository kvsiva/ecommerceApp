package com.example.ecommerce.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventEnvelopeTest {
    @Test
    void createsEnvelopeWithGeneratedMetadata() {
        var payload = new OrderEvents.OrderConfirmed("order-1");

        var envelope = EventEnvelope.of("OrderConfirmed", "order-1", payload);

        assertThat(envelope.eventId()).isNotBlank();
        assertThat(envelope.eventType()).isEqualTo("OrderConfirmed");
        assertThat(envelope.correlationId()).isEqualTo("order-1");
        assertThat(envelope.occurredAt()).isNotNull();
        assertThat(envelope.payload()).isEqualTo(payload);
    }
}
