package com.retailable.supermarket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class SecurityIntegrationIT {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void loginLogoutAndAuthorizationAreEnforced() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/inventory")).andExpect(status().isUnauthorized());

        String member = login("member", "Passw0rd!");
        mvc.perform(get("/api/inventory").header("Authorization", "Bearer " + member))
                .andExpect(status().isForbidden());

        String admin = login("admin", "Passw0rd!");
        mvc.perform(get("/api/admin/roles").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());
        mvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());
        mvc.perform(get("/api/admin/roles").header("Authorization", "Bearer " + admin))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void memberCannotReadAnotherMembersOrder() throws Exception {
        String suffix = Long.toString(System.nanoTime());
        String user1 = "m" + suffix.substring(Math.max(0, suffix.length()-10));
        String user2 = "n" + suffix.substring(Math.max(0, suffix.length()-10));
        register(user1); register(user2);
        String token1 = login(user1, "Passw0rd!");
        String token2 = login(user2, "Passw0rd!");
        String body = "{\"idempotencyKey\":\"test-"+suffix+"\",\"items\":[{\"productId\":1,\"quantity\":1}]}";
        String response = mvc.perform(post("/api/orders").header("Authorization", "Bearer " + token1)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long orderId = json.readTree(response).path("data").path("id").asLong();
        mvc.perform(get("/api/orders/" + orderId).header("Authorization", "Bearer " + token2))
                .andExpect(status().isForbidden());
    }

    private void register(String username) throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\""+username+"\",\"password\":\"Passw0rd!\",\"displayName\":\"测试会员\"}"))
                .andExpect(status().isOk());
    }

    private String login(String username, String password) throws Exception {
        String response = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\""+username+"\",\"password\":\""+password+"\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode root = json.readTree(response);
        return root.path("data").path("token").asText();
    }
}
