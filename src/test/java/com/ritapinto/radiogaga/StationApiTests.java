package com.ritapinto.radiogaga;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "radiogaga.api-key=test-key")
@AutoConfigureMockMvc
class StationApiTests {

    private static final String STATION = "{\"name\":\"Classic Rock\"}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createWithoutApiKeyIsRejected() throws Exception {
        mockMvc.perform(post("/stations").contentType(MediaType.APPLICATION_JSON).content(STATION))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createWithWrongApiKeyIsRejected() throws Exception {
        mockMvc.perform(post("/stations").header("X-API-Key", "wrong")
                        .contentType(MediaType.APPLICATION_JSON).content(STATION))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createWithValidApiKeySucceeds() throws Exception {
        mockMvc.perform(post("/stations").header("X-API-Key", "test-key")
                        .contentType(MediaType.APPLICATION_JSON).content(STATION))
                .andExpect(status().isCreated());
    }

    @Test
    void listingStationsIsPublic() throws Exception {
        mockMvc.perform(get("/stations")).andExpect(status().isOk());
    }

    @Test
    void blankNameIsRejected() throws Exception {
        mockMvc.perform(post("/stations").header("X-API-Key", "test-key")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownStationReturnsNotFound() throws Exception {
        mockMvc.perform(get("/stations/999")).andExpect(status().isNotFound());
    }
}