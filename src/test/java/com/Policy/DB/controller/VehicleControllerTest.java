package com.Policy.DB.controller;

import com.Policy.DB.model.Vehicle;
import com.Policy.DB.model.VehicleType;
import com.Policy.DB.service.VehicleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehicleController.class)
@AutoConfigureMockMvc(addFilters = false)
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VehicleService vehicleService;

    @Autowired
    private ObjectMapper objectMapper;

    // -------- Add Vehicle --------
    @Test
    void addVehicle_success() throws Exception {
        Vehicle vehicle = new Vehicle(
                1, null, "TS09AB1234",
                "Honda", "City", 2022,
                VehicleType.CAR, null
        );

        Mockito.when(vehicleService.addVehicle(Mockito.any(Vehicle.class), Mockito.eq(1)))
                .thenReturn(vehicle);

        mockMvc.perform(post("/vehicles/add/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicle)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registrationNumber").value("TS09AB1234"))
                .andExpect(jsonPath("$.make").value("Honda"));
    }

    // -------- Add Vehicle - Bad Request --------
    @Test
    void addVehicle_failure() throws Exception {
        Mockito.when(vehicleService.addVehicle(Mockito.any(Vehicle.class), Mockito.eq(99)))
                .thenThrow(new RuntimeException("Customer not found"));

        mockMvc.perform(post("/vehicles/add/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Vehicle())))
                .andExpect(status().isBadRequest());
    }

    // -------- Get Vehicle By ID --------
    @Test
    void getVehicleDetails_success() throws Exception {
        Vehicle vehicle = new Vehicle(
                1, null, "TS09AB1234",
                "Honda", "City", 2022,
                VehicleType.CAR, null
        );

        Mockito.when(vehicleService.getVehicleDetails(1)).thenReturn(vehicle);

        mockMvc.perform(get("/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("City"));
    }

    // -------- Get Vehicle By ID - Not Found --------
    @Test
    void getVehicleDetails_notFound() throws Exception {
        Mockito.when(vehicleService.getVehicleDetails(99))
                .thenThrow(new RuntimeException("Vehicle not found"));

        mockMvc.perform(get("/vehicles/99"))
                .andExpect(status().isNotFound());
    }

    // -------- Update Vehicle --------
    @Test
    void updateVehicle_success() throws Exception {
        Vehicle updatedVehicle = new Vehicle(
                1, null, "TS09AB9999",
                "Hyundai", "Creta", 2023,
                VehicleType.CAR, null
        );

        Mockito.when(vehicleService.updateVehicleDetails(Mockito.eq(1), Mockito.any(Vehicle.class)))
                .thenReturn(updatedVehicle);

        mockMvc.perform(put("/vehicles/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedVehicle)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.make").value("Hyundai"))
                .andExpect(jsonPath("$.model").value("Creta"));
    }

    // -------- Get Vehicles By Customer --------
    @Test
    void getVehiclesByCustomer_success() throws Exception {
        List<Vehicle> vehicles = List.of(
                new Vehicle(1, null, "TS01", "Honda", "City", 2022, VehicleType.CAR, null),
                new Vehicle(2, null, "TS02", "Yamaha", "R15", 2021, VehicleType.BIKE, null)
        );

        Mockito.when(vehicleService.getVehiclesByCustomerId(1))
                .thenReturn(vehicles);

        mockMvc.perform(get("/vehicles/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    // -------- Get All Vehicles --------
    @Test
    void getAllVehicles_success() throws Exception {
        List<Vehicle> vehicles = List.of(
                new Vehicle(1, null, "TS01", "Honda", "City", 2022, VehicleType.CAR, null)
        );

        Mockito.when(vehicleService.getAllVehicles()).thenReturn(vehicles);

        mockMvc.perform(get("/vehicles/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    // -------- Delete Vehicle --------
    @Test
    void deleteVehicle_success() throws Exception {
        Mockito.doNothing().when(vehicleService).deleteVehicle(1);

        mockMvc.perform(delete("/vehicles/delete/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Vehicle deleted successfully"));
    }

    // -------- Delete Vehicle - Not Found --------
    @Test
    void deleteVehicle_notFound() throws Exception {
        Mockito.doThrow(new RuntimeException("Vehicle not found"))
                .when(vehicleService).deleteVehicle(99);

        mockMvc.perform(delete("/vehicles/delete/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Vehicle not found"));
    }
}
