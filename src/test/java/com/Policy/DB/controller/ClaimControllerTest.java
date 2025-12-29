package com.Policy.DB.controller;

import com.Policy.DB.model.*;
import com.Policy.DB.service.ClaimService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClaimController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClaimControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClaimService claimService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fileClaim_success() throws Exception {
        Claim claim = new Claim();
        claim.setClaimAmount(BigDecimal.valueOf(5000));
        claim.setClaimDate(LocalDate.now());

        Mockito.when(claimService.fileClaim(Mockito.any(), Mockito.eq(1)))
                .thenReturn(claim);

        mockMvc.perform(post("/claims/file/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(claim)))
                .andExpect(status().isCreated());
    }

    @Test
    void getClaimStatus_success() throws Exception {
        Claim claim = new Claim();
        claim.setClaimId(1);

        Mockito.when(claimService.getClaimStatus(1)).thenReturn(claim);

        mockMvc.perform(get("/claims/1"))
                .andExpect(status().isOk());
    }

    @Test
    void processClaim_success() throws Exception {
        Claim claim = new Claim();
        claim.setClaimStatus(ClaimStatus.APPROVED);

        Mockito.when(claimService.processClaim(1, ClaimStatus.APPROVED))
                .thenReturn(claim);

        mockMvc.perform(put("/claims/process/1")
                        .param("status", "APPROVED"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllClaims_success() throws Exception {
        Mockito.when(claimService.getAllClaims())
                .thenReturn(List.of(new Claim()));

        mockMvc.perform(get("/claims/all"))
                .andExpect(status().isOk());
    }

    @Test
    void getClaimsByPolicyId_success() throws Exception {
        Mockito.when(claimService.getClaimsByPolicyId(1))
                .thenReturn(List.of(new Claim()));

        mockMvc.perform(get("/claims/policy/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getClaimsByCustomerId_success() throws Exception {
        Mockito.when(claimService.getClaimsByCustomerId(1))
                .thenReturn(List.of(new Claim()));

        mockMvc.perform(get("/claims/customer/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getPendingClaims_success() throws Exception {
        Mockito.when(claimService.getPendingClaims())
                .thenReturn(List.of(new Claim()));

        mockMvc.perform(get("/claims/pending"))
                .andExpect(status().isOk());
    }

    @Test
    void getClaimsByStatus_success() throws Exception {
        Mockito.when(claimService.getClaimsByStatus(ClaimStatus.SUBMITTED))
                .thenReturn(List.of(new Claim()));

        mockMvc.perform(get("/claims/status/SUBMITTED"))
                .andExpect(status().isOk());
    }
}
