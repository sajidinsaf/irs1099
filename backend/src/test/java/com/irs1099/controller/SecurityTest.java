package com.irs1099.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.irs1099.dto.request.RegisterRequest;
import com.irs1099.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void publicEndpoints_areAccessibleWithoutAuth() throws Exception {
        // Health may return 503 (DOWN) if mail is unconfigured, but must not return 401/403
        int statusCode = mockMvc.perform(get("/actuator/health"))
                .andReturn().getResponse().getStatus();
        assertNotEquals(401, statusCode);
        assertNotEquals(403, statusCode);
    }

    @Test
    void authEndpoints_areAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@b.com\",\"password\":\"test\"}"))
                .andExpect(status().isUnauthorized()); // 401, not 403
    }

    @Test
    void protectedEndpoints_return403_withoutToken() throws Exception {
        mockMvc.perform(get("/dashboard/summary"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoints_return403_withInvalidToken() throws Exception {
        mockMvc.perform(get("/dashboard/summary")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoints_succeed_withValidToken() throws Exception {
        // Register to get a token
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@example.com");
        req.setPassword("Test1234!");
        req.setFirstName("Test");
        req.setLastName("User");

        String response = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(response).get("accessToken").asText();

        // Access protected endpoint
        mockMvc.perform(get("/dashboard/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSubmissions").value(0))
                .andExpect(jsonPath("$.unreadNotifications").value(0));
    }
}
