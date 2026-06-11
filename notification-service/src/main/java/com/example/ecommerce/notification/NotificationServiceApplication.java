package com.example.ecommerce.notification;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

    @RestController
    @RequestMapping("/notifications")
    static class NotificationController {
        private final Map<String, NotificationMessage> notifications = new ConcurrentHashMap<>();

        @GetMapping
        Collection<NotificationMessage> list() {
            return notifications.values();
        }

        @PostMapping
        NotificationMessage send(@Valid @RequestBody SendNotification request) {
            var message = new NotificationMessage(
                    UUID.randomUUID().toString(),
                    request.customerId(),
                    request.channel(),
                    request.subject(),
                    request.body(),
                    Instant.now(),
                    "SENT"
            );
            notifications.put(message.notificationId(), message);
            return message;
        }
    }

    record SendNotification(@NotBlank String customerId, @NotBlank String channel, @NotBlank String subject, @NotBlank String body) {}
    record NotificationMessage(String notificationId, String customerId, String channel, String subject, String body, Instant sentAt, String status) {}
}
