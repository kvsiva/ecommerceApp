package com.example.ecommerce.notification;

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
class NotificationServiceApplicationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void sendsNotification() throws Exception {
        mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId":"customer-1",
                                  "channel":"EMAIL",
                                  "subject":"Order confirmed",
                                  "body":"Your order has been confirmed."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId", not(blankOrNullString())))
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.channel").value("EMAIL"));
    }
}
