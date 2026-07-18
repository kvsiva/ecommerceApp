package com.example.ecommerce.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InventoryServiceApplicationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void reservesAndReleasesStock() throws Exception {
        mockMvc.perform(put("/inventory/test-product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"available":5}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(5));

        var reservationId = mockMvc.perform(post("/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId":"order-1",
                                  "items":[{"productId":"test-product","quantity":3,"unitPrice":10.00}]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId", not(blankOrNullString())))
                .andReturn()
                .getResponse()
                .getContentAsString()
                .split("\"reservationId\":\"")[1]
                .split("\"")[0];

        mockMvc.perform(post("/inventory/release/{reservationId}", reservationId))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsReservationWhenStockIsInsufficient() throws Exception {
        mockMvc.perform(put("/inventory/low-stock-product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"available":1}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId":"order-2",
                                  "items":[{"productId":"low-stock-product","quantity":2,"unitPrice":10.00}]
                                }
                                """))
                .andExpect(status().isConflict());
    }
}
