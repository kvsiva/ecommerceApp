package com.example.ecommerce.payment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentServiceApplicationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void processesSuccessfulPayment() throws Exception {
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":"order-1","amount":199.99,"forceFailure":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId", not(blankOrNullString())))
                .andExpect(jsonPath("$.status").value("SUCCEEDED"));
    }

    @Test
    void recordsFailedPayment() throws Exception {
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":"order-2","amount":199.99,"forceFailure":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"));
    }
}
