package com.jobflow.backend.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.backend.domain.ApplicationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createsAndListsApplicationsThroughRestApi() throws Exception {
        CreateApplicationRequest request = new CreateApplicationRequest(
                "Bruce HR",
                "Java Backend Engineer",
                "Remote",
                true,
                "Spring API project"
        );

        mockMvc.perform(post("/api/applications")
                        .header("Idempotency-Key", "controller-create-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.status", is("SOURCED")))
                .andExpect(jsonPath("$.remote", is(true)));

        mockMvc.perform(get("/api/applications")
                        .param("remote", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].company", is("Bruce HR")));
    }

    @Test
    void returnsConflictForIllegalStatusTransition() throws Exception {
        CreateApplicationRequest request = new CreateApplicationRequest(
                "Bruce HR",
                "Java Backend Engineer",
                "Remote",
                true,
                "Status transition"
        );

        String response = mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(patch("/api/applications/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangeStatusRequest(ApplicationStatus.OFFER))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("illegal_status_transition")));
    }
}
