package com.example.ecommerce.product;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class ProductServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }

    @RestController
    @RequestMapping("/products")
    static class ProductController {
        private final Map<String, Product> products = new ConcurrentHashMap<>();

        @PostConstruct
        void seed() {
            create(new UpsertProduct("Laptop", "Electronics", new BigDecimal("799.00"), "Portable work machine"));
            create(new UpsertProduct("Headphones", "Electronics", new BigDecimal("99.00"), "Wireless audio"));
            create(new UpsertProduct("Backpack", "Travel", new BigDecimal("49.00"), "Daily carry bag"));
        }

        @GetMapping
        Collection<Product> list(@RequestParam(required = false) String q) {
            if (q == null || q.isBlank()) {
                return products.values();
            }
            var query = q.toLowerCase();
            return products.values().stream()
                    .filter(product -> product.name().toLowerCase().contains(query) || product.category().toLowerCase().contains(query))
                    .toList();
        }

        @GetMapping("/{id}")
        Product get(@PathVariable String id) {
            var product = products.get(id);
            if (product == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
            }
            return product;
        }

        @PostMapping
        Product create(@Valid @RequestBody UpsertProduct request) {
            var id = UUID.randomUUID().toString();
            var product = new Product(id, request.name(), request.category(), request.price(), request.description());
            products.put(id, product);
            return product;
        }

        @PutMapping("/{id}")
        Product update(@PathVariable String id, @Valid @RequestBody UpsertProduct request) {
            if (!products.containsKey(id)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
            }
            var product = new Product(id, request.name(), request.category(), request.price(), request.description());
            products.put(id, product);
            return product;
        }
    }

    record UpsertProduct(@NotBlank String name, @NotBlank String category, @Positive BigDecimal price, String description) {}
    record Product(String id, String name, String category, BigDecimal price, String description) {}
}
