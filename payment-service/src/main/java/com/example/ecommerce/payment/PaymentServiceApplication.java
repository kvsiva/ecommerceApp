package com.example.ecommerce.payment;

import com.example.ecommerce.common.EventEnvelope;
import com.example.ecommerce.common.OrderEvents;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class PaymentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }

    @RestController
    @RequestMapping("/payments")
    static class PaymentController {
        private final Map<String, Payment> payments = new ConcurrentHashMap<>();
        private final ApplicationEventPublisher publisher;
        private final Logger log = LoggerFactory.getLogger(PaymentController.class);

        PaymentController(ApplicationEventPublisher publisher) {
            this.publisher = publisher;
        }

        @GetMapping
        Collection<Payment> list() {
            return payments.values();
        }

        @PostMapping
        Payment process(@Valid @RequestBody PaymentRequest request) {
            var status = request.forceFailure() ? PaymentStatus.FAILED : PaymentStatus.SUCCEEDED;
            var payment = new Payment(UUID.randomUUID().toString(), request.orderId(), request.amount(), status);
            payments.put(payment.paymentId(), payment);
            if (status == PaymentStatus.SUCCEEDED) {
                publish("payment.succeeded", EventEnvelope.of("PaymentSucceeded", request.orderId(),
                        new OrderEvents.PaymentSucceeded(request.orderId(), payment.paymentId(), request.amount())));
            } else {
                publish("payment.failed", EventEnvelope.of("PaymentFailed", request.orderId(),
                        new OrderEvents.PaymentFailed(request.orderId(), "Payment gateway declined transaction")));
            }
            return payment;
        }

        private void publish(String topic, EventEnvelope<?> event) {
            publisher.publishEvent(event);
            log.info("Published {} to {}", event.eventType(), topic);
        }
    }

    enum PaymentStatus { SUCCEEDED, FAILED }
    record PaymentRequest(@NotBlank String orderId, @Positive BigDecimal amount, boolean forceFailure) {}
    record Payment(String paymentId, String orderId, BigDecimal amount, PaymentStatus status) {}
}
