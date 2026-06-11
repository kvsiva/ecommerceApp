package com.example.ecommerce.inventory;

import com.example.ecommerce.common.OrderEvents;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class InventoryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }

    @RestController
    @RequestMapping("/inventory")
    static class InventoryController {
        private final Map<String, Stock> stock = new ConcurrentHashMap<>();
        private final Map<String, Reservation> reservations = new ConcurrentHashMap<>();

        @PostConstruct
        void seed() {
            stock.put("demo-laptop", new Stock("demo-laptop", 20));
            stock.put("demo-headphones", new Stock("demo-headphones", 50));
        }

        @GetMapping
        Collection<Stock> list() {
            return stock.values();
        }

        @PutMapping("/{productId}")
        Stock upsert(@PathVariable String productId, @Valid @RequestBody UpdateStock request) {
            var next = new Stock(productId, request.available());
            stock.put(productId, next);
            return next;
        }

        @PostMapping("/reserve")
        Reservation reserve(@Valid @RequestBody ReserveStock request) {
            for (var item : request.items()) {
                var current = stock.getOrDefault(item.productId(), new Stock(item.productId(), 0));
                if (current.available() < item.quantity()) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock for " + item.productId());
                }
            }
            request.items().forEach(item -> stock.compute(item.productId(),
                    (id, current) -> new Stock(id, current.available() - item.quantity())));
            var reservation = new Reservation(UUID.randomUUID().toString(), request.orderId(), request.items());
            reservations.put(reservation.reservationId(), reservation);
            return reservation;
        }

        @PostMapping("/release/{reservationId}")
        void release(@PathVariable String reservationId) {
            var reservation = reservations.remove(reservationId);
            if (reservation != null) {
                reservation.items().forEach(item -> stock.compute(item.productId(),
                        (id, current) -> new Stock(id, (current == null ? 0 : current.available()) + item.quantity())));
            }
        }
    }

    record UpdateStock(@PositiveOrZero int available) {}
    record ReserveStock(@NotBlank String orderId, List<OrderEvents.OrderItem> items) {}
    record Stock(String productId, int available) {}
    record Reservation(String reservationId, String orderId, List<OrderEvents.OrderItem> items) {}
}
