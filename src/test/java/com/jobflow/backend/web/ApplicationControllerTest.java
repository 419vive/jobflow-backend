package com.jobflow.backend.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.backend.application.ApplicationSummaryCache;
import com.jobflow.backend.domain.ApplicationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApplicationSummaryCache summaryCache;

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
    void returnsOkAndSameApplicationForRepeatedIdempotencyKey() throws Exception {
        CreateApplicationRequest firstRequest = new CreateApplicationRequest(
                "Bruce HR",
                "Java Backend Engineer",
                "Remote",
                true,
                "First request"
        );
        CreateApplicationRequest secondRequest = new CreateApplicationRequest(
                "Different Company",
                "Different Title",
                "Remote",
                true,
                "Duplicate request"
        );

        String firstResponse = mockMvc.perform(post("/api/applications")
                        .header("Idempotency-Key", "controller-duplicate-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String firstId = objectMapper.readTree(firstResponse).get("id").asText();

        mockMvc.perform(post("/api/applications")
                        .header("Idempotency-Key", "controller-duplicate-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(firstId)))
                .andExpect(jsonPath("$.company", is("Bruce HR")));
    }

    @Test
    void listsNewestApplicationsFirstByDefault() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .header("Idempotency-Key", "older-application")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateApplicationRequest(
                                "Older Company",
                                "Java Backend Engineer",
                                "Remote",
                                true,
                                "First"
                        ))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/applications")
                        .header("Idempotency-Key", "newer-application")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateApplicationRequest(
                                "Newer Company",
                                "Java Backend Engineer",
                                "Remote",
                                true,
                                "Second"
                        ))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].company", is("Newer Company")))
                .andExpect(jsonPath("$.content[1].company", is("Older Company")));
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
