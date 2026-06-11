package com.example.ecommerce.common;

import java.math.BigDecimal;
import java.util.List;

public final class OrderEvents {
    private OrderEvents() {
    }

    public record OrderItem(String productId, int quantity, BigDecimal unitPrice) {
    }

    public record OrderCreated(String orderId, String customerId, List<OrderItem> items, BigDecimal total) {
    }

    public record StockReserved(String orderId, String reservationId) {
    }

    public record StockReservationFailed(String orderId, String reason) {
    }

    public record PaymentSucceeded(String orderId, String paymentId, BigDecimal amount) {
    }

    public record PaymentFailed(String orderId, String reason) {
    }

    public record OrderConfirmed(String orderId) {
    }

    public record OrderCancelled(String orderId, String reason) {
    }

    public record NotificationRequested(String customerId, String channel, String subject, String body) {
    }
}
