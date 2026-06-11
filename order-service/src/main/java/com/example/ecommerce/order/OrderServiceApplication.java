package com.example.ecommerce.order;

import com.example.ecommerce.common.EventEnvelope;
import com.example.ecommerce.common.OrderEvents;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

    @RestController
    @RequestMapping("/orders")
    static class OrderController {
        private final Map<String, CustomerOrder> orders = new ConcurrentHashMap<>();
        private final DomainEvents events;

        OrderController(DomainEvents events) {
            this.events = events;
        }

        @GetMapping
        Collection<CustomerOrder> list() {
            return orders.values();
        }

        @GetMapping("/{orderId}")
        CustomerOrder get(@PathVariable String orderId) {
            var order = orders.get(orderId);
            if (order == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
            }
            return order;
        }

        @PostMapping
        CustomerOrder create(@Valid @RequestBody CreateOrder request) {
            var orderId = UUID.randomUUID().toString();
            var total = request.items().stream()
                    .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            var order = new CustomerOrder(orderId, request.customerId(), OrderStatus.PENDING, request.items(), total);
            orders.put(orderId, order);
            events.publish("order.created", EventEnvelope.of("OrderCreated", orderId,
                    new OrderEvents.OrderCreated(orderId, request.customerId(), request.items(), total)));
            return order;
        }

        @PostMapping("/{orderId}/confirm")
        CustomerOrder confirm(@PathVariable String orderId) {
            var order = get(orderId);
            var confirmed = order.withStatus(OrderStatus.CONFIRMED);
            orders.put(orderId, confirmed);
            events.publish("order.confirmed", EventEnvelope.of("OrderConfirmed", orderId, new OrderEvents.OrderConfirmed(orderId)));
            return confirmed;
        }

        @PostMapping("/{orderId}/cancel")
        CustomerOrder cancel(@PathVariable String orderId, @RequestBody(required = false) CancelOrder request) {
            var order = get(orderId);
            var cancelled = order.withStatus(OrderStatus.CANCELLED);
            orders.put(orderId, cancelled);
            var reason = request == null ? "Cancelled" : request.reason();
            events.publish("order.cancelled", EventEnvelope.of("OrderCancelled", orderId, new OrderEvents.OrderCancelled(orderId, reason)));
            return cancelled;
        }
    }

    @Service
    static class DomainEvents {
        private static final Logger log = LoggerFactory.getLogger(DomainEvents.class);
        private final ApplicationEventPublisher publisher;

        DomainEvents(ApplicationEventPublisher publisher) {
            this.publisher = publisher;
        }

        void publish(String topic, EventEnvelope<?> event) {
            publisher.publishEvent(event);
            log.info("Published {} to {} with correlationId={}", event.eventType(), topic, event.correlationId());
        }
    }

    enum OrderStatus { PENDING, CONFIRMED, CANCELLED }
    record CreateOrder(@NotBlank String customerId, @NotEmpty List<OrderEvents.OrderItem> items) {}
    record CancelOrder(String reason) {}
    record CustomerOrder(String orderId, String customerId, OrderStatus status, List<OrderEvents.OrderItem> items, @Positive BigDecimal total) {
        CustomerOrder withStatus(OrderStatus next) {
            return new CustomerOrder(orderId, customerId, next, items, total);
        }
    }
}
