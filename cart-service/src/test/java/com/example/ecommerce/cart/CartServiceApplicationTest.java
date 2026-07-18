package com.example.ecommerce.cart;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CartServiceApplicationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void addsAndClearsCartItems() throws Exception {
        mockMvc.perform(post("/carts/customer-1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"demo-laptop","quantity":2,"unitPrice":799.00}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("customer-1"))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        mockMvc.perform(delete("/carts/customer-1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/carts/customer-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));
    }
}
