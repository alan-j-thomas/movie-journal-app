package com.demo.journalservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.demo.journalservice.dto.JournalRequest;
import com.demo.journalservice.entity.Journal;
import com.demo.journalservice.service.JournalServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;


@WebMvcTest(JournalController.class)
class JournalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JournalServiceImpl journalService;

    @Autowired
    private ObjectMapper objectMapper;

    private Journal mockJournal;
    private JournalRequest mockRequest;

    @BeforeEach
    void setup() {
        mockJournal = new Journal();
        mockJournal.setJournalId(1);
        mockJournal.setUserId(100);
        mockJournal.setMovieId(200);
        mockJournal.setTitle("Test Title");
        mockJournal.setContent("Test Content");
        mockJournal.setMoodTag("Happy");

        mockRequest = new JournalRequest("Inception", 100, "Test Title", "Test Content", "Happy", 200);
    }
    
    @Test
    void testAddJournal_Valid() throws Exception {
        when(journalService.addJournal(any(Journal.class))).thenReturn(mockJournal);

        mockMvc.perform(post("/journals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockJournal)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.journalId").value(1));
    }
    
    @Test
    void testAddToJournal_Valid() throws Exception {
        when(journalService.addToJournal(any(JournalRequest.class))).thenReturn(mockJournal);

        mockMvc.perform(post("/journals/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.journalId").value(1));
    }
    
    @Test
    void testGetAllJournals() throws Exception {
        when(journalService.getAllJournals()).thenReturn(List.of(mockJournal));

        mockMvc.perform(get("/journals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].journalId").value(1));
    }
    
    @Test
    void testGetJournalById() throws Exception {
        when(journalService.getJournal(1)).thenReturn(mockJournal);

        mockMvc.perform(get("/journals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.journalId").value(1));
    }
    
    @Test
    void testUpdateJournal() throws Exception {
        when(journalService.updateJournal(eq(1), any(JournalRequest.class))).thenReturn(mockJournal);

        mockMvc.perform(put("/journals/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.journalId").value(1));
    }
    
    @Test
    void testGetJournalsByUser() throws Exception {
        when(journalService.getJournalsByUser(100)).thenReturn(List.of(mockJournal));

        mockMvc.perform(get("/journals/users/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(100));
    }
    
    @Test
    void testDeleteJournal() throws Exception {
        when(journalService.deleteJournal(1)).thenReturn("Journal deleted Successfully!");

        mockMvc.perform(delete("/journals/1"))
                .andExpect(status().isOk());
    }
    
    @Test
    void testAddJournal_InvalidInput_ShouldReturnBadRequest() throws Exception {
        Journal invalidJournal = new Journal(); // Missing required fields

        mockMvc.perform(post("/journals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidJournal)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("should not be blank")));
    }
    
    @Test
    void testAddToJournal_InvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Create an invalid JournalRequest (missing required fields)
        JournalRequest invalidRequest = new JournalRequest();
        // All fields are null or blank

        mockMvc.perform(post("/journals/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("should not be blank")));
    }


}
