package com.imageng;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imageng.model.GenerateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ImageApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.app", containsString("Java Spring Boot")));
    }

    @Test
    public void testGetStyles() throws Exception {
        mockMvc.perform(get("/api/styles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].name", notNullValue()))
                .andExpect(jsonPath("$[0].value", notNullValue()));
    }

    @Test
    public void testGetAspectRatios() throws Exception {
        mockMvc.perform(get("/api/aspect-ratios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].name", is("Square")));
    }

    @Test
    public void testGetExamples() throws Exception {
        mockMvc.perform(get("/api/examples"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    public void testGenerateImageSuccess() throws Exception {
        GenerateRequest request = new GenerateRequest("A majestic dragon over the mountains", "Anime", "16:9");

        mockMvc.perform(post("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.imageUrl", notNullValue()))
                .andExpect(jsonPath("$.prompt", is("A majestic dragon over the mountains")));
    }

    @Test
    public void testGenerateImageValidationFailure() throws Exception {
        GenerateRequest emptyRequest = new GenerateRequest("", "Anime", "16:9");

        mockMvc.perform(post("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testHistoryAndClear() throws Exception {
        // Fetch history
        mockMvc.perform(get("/api/history"))
                .andExpect(status().isOk());

        // Clear history
        mockMvc.perform(delete("/api/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("History cleared")));
    }
}
