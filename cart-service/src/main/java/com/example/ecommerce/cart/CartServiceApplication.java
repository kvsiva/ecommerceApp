package com.example.ecommerce.cart;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class CartServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CartServiceApplication.class, args);
    }

    @RestController
    @RequestMapping("/carts")
    static class CartController {
        private final Map<String, Cart> carts = new ConcurrentHashMap<>();

        @GetMapping("/{customerId}")
        Cart get(@PathVariable String customerId) {
            return carts.computeIfAbsent(customerId, Cart::empty);
        }

        @PostMapping("/{customerId}/items")
        Cart addItem(@PathVariable String customerId, @Valid @RequestBody AddItem request) {
            var cart = carts.computeIfAbsent(customerId, Cart::empty);
            var items = new ArrayList<>(cart.items());
            items.add(new CartItem(request.productId(), request.quantity(), request.unitPrice()));
            var updated = new Cart(customerId, List.copyOf(items));
            carts.put(customerId, updated);
            return updated;
        }

        @DeleteMapping("/{customerId}")
        void clear(@PathVariable String customerId) {
            carts.remove(customerId);
        }
    }

    record AddItem(@NotBlank String productId, @Positive int quantity, @Positive java.math.BigDecimal unitPrice) {}
    record CartItem(String productId, int quantity, java.math.BigDecimal unitPrice) {}
    record Cart(String customerId, List<CartItem> items) {
        static Cart empty(String customerId) {
            return new Cart(customerId, List.of());
        }
    }
}
