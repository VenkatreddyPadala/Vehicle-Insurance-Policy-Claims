package com.Policy.DB.controller;
import com.Policy.DB.service.VehicleService;
import com.Policy.DB.model.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/vehicles")
@CrossOrigin(origins = "*")
public class VehicleController {
    @Autowired
    private VehicleService vehicleService;

    // Add new vehicle
    @PostMapping("/add/{customerId}")
    public ResponseEntity<Vehicle> addVehicle(
            @RequestBody Vehicle vehicle,
            @PathVariable Integer customerId) {
        try {
            Vehicle savedVehicle = vehicleService.addVehicle(vehicle, customerId);
            return new ResponseEntity<>(savedVehicle, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Get vehicle details
    @GetMapping("/{vehicleId}")
    public ResponseEntity<Vehicle> getVehicleDetails(@PathVariable Integer vehicleId) {
        try {
            Vehicle vehicle = vehicleService.getVehicleDetails(vehicleId);
            return new ResponseEntity<>(vehicle, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Update vehicle details
    @PutMapping("/update/{vehicleId}")
    public ResponseEntity<Vehicle> updateVehicleDetails(
            @PathVariable Integer vehicleId,
            @RequestBody Vehicle vehicle) {
        try {
            Vehicle updatedVehicle = vehicleService.updateVehicleDetails(vehicleId, vehicle);
            return new ResponseEntity<>(updatedVehicle, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Get all vehicles for a customer
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Vehicle>> getVehiclesByCustomerId(@PathVariable Integer customerId) {
        List<Vehicle> vehicles = vehicleService.getVehiclesByCustomerId(customerId);
        return new ResponseEntity<>(vehicles, HttpStatus.OK);
    }

    // Get all vehicles
    @GetMapping("/all")
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        List<Vehicle> vehicles = vehicleService.getAllVehicles();
        return new ResponseEntity<>(vehicles, HttpStatus.OK);
    }

    // Delete vehicle
    @DeleteMapping("/delete/{vehicleId}")
    public ResponseEntity<String> deleteVehicle(@PathVariable Integer vehicleId) {
        try {
            vehicleService.deleteVehicle(vehicleId);
            return new ResponseEntity<>("Vehicle deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Vehicle not found", HttpStatus.NOT_FOUND);
        }
    }
}
