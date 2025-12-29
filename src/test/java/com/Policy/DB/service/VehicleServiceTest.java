package com.Policy.DB.service;

import com.Policy.DB.model.Customer;
import com.Policy.DB.model.Vehicle;
import com.Policy.DB.model.VehicleType;
import com.Policy.DB.repository.CustomerRepository;
import com.Policy.DB.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private VehicleService vehicleService;

    // -------- Add Vehicle --------
    @Test
    void addVehicle_success() {
        Customer customer = new Customer(
                1, "Venkat", "venkat@gmail.com",
                "9876543210", "Hyderabad", null
        );

        Vehicle vehicle = new Vehicle(
                null, null, "TS09AB1234",
                "Honda", "City", 2022,
                VehicleType.CAR, null
        );

        when(customerRepository.findById(1))
                .thenReturn(Optional.of(customer));
        when(vehicleRepository.existsByRegistrationNumber("TS09AB1234"))
                .thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class)))
                .thenReturn(vehicle);

        Vehicle savedVehicle = vehicleService.addVehicle(vehicle, 1);

        assertNotNull(savedVehicle);
        assertEquals("Honda", savedVehicle.getMake());
        verify(vehicleRepository, times(1)).save(vehicle);
    }

    // -------- Add Vehicle - Customer Not Found --------
    @Test
    void addVehicle_customerNotFound() {
        Vehicle vehicle = new Vehicle();

        when(customerRepository.findById(99))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> vehicleService.addVehicle(vehicle, 99));

        assertEquals("Customer not found with id: 99", ex.getMessage());
    }

    // -------- Add Vehicle - Duplicate Registration --------
    @Test
    void addVehicle_duplicateRegistration() {
        Customer customer = new Customer();
        Vehicle vehicle = new Vehicle();
        vehicle.setRegistrationNumber("TS09AB1234");

        when(customerRepository.findById(1))
                .thenReturn(Optional.of(customer));
        when(vehicleRepository.existsByRegistrationNumber("TS09AB1234"))
                .thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> vehicleService.addVehicle(vehicle, 1));

        assertEquals("Vehicle with this registration number already exists", ex.getMessage());
    }

    // -------- Get Vehicle Details --------
    @Test
    void getVehicleDetails_success() {
        Vehicle vehicle = new Vehicle(
                1, null, "TS09AB1234",
                "Honda", "City", 2022,
                VehicleType.CAR, null
        );

        when(vehicleRepository.findById(1))
                .thenReturn(Optional.of(vehicle));

        Vehicle result = vehicleService.getVehicleDetails(1);

        assertEquals("City", result.getModel());
    }

    // -------- Get Vehicle Details - Not Found --------
    @Test
    void getVehicleDetails_notFound() {
        when(vehicleRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> vehicleService.getVehicleDetails(99));
    }

    // -------- Update Vehicle --------
    @Test
    void updateVehicle_success() {
        Vehicle existing = new Vehicle(
                1, null, "TS01",
                "Honda", "City", 2021,
                VehicleType.CAR, null
        );

        Vehicle update = new Vehicle(
                null, null, "TS02",
                "Hyundai", "Creta", 2023,
                VehicleType.CAR, null
        );

        when(vehicleRepository.findById(1))
                .thenReturn(Optional.of(existing));
        when(vehicleRepository.save(any(Vehicle.class)))
                .thenReturn(existing);

        Vehicle result = vehicleService.updateVehicleDetails(1, update);

        assertEquals("Hyundai", result.getMake());
        assertEquals("Creta", result.getModel());
    }

    // -------- Get Vehicles By Customer --------
    @Test
    void getVehiclesByCustomerId_success() {
        List<Vehicle> vehicles = List.of(
                new Vehicle(1, null, "TS01", "Honda", "City", 2022, VehicleType.CAR, null),
                new Vehicle(2, null, "TS02", "Yamaha", "R15", 2021, VehicleType.BIKE, null)
        );

        when(vehicleRepository.findByCustomer_CustomerId(1))
                .thenReturn(vehicles);

        List<Vehicle> result = vehicleService.getVehiclesByCustomerId(1);

        assertEquals(2, result.size());
    }

    // -------- Get All Vehicles --------
    @Test
    void getAllVehicles_success() {
        when(vehicleRepository.findAll())
                .thenReturn(List.of(new Vehicle()));

        List<Vehicle> result = vehicleService.getAllVehicles();

        assertEquals(1, result.size());
    }

    // -------- Delete Vehicle --------
    @Test
    void deleteVehicle_success() {
        when(vehicleRepository.existsById(1)).thenReturn(true);

        vehicleService.deleteVehicle(1);

        verify(vehicleRepository, times(1)).deleteById(1);
    }

    // -------- Delete Vehicle - Not Found --------
    @Test
    void deleteVehicle_notFound() {
        when(vehicleRepository.existsById(99)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> vehicleService.deleteVehicle(99));

        assertEquals("Vehicle not found with this id: 99", ex.getMessage());
    }
}
