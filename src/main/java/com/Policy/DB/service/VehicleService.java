package com.Policy.DB.service;

import com.Policy.DB.model.Customer;
import com.Policy.DB.model.Vehicle;
import com.Policy.DB.repository.CustomerRepository;
import com.Policy.DB.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private CustomerRepository customerRepository;

    // Add a new Vehicle
    public Vehicle addVehicle(Vehicle vehicle,Integer customerId){
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(()-> new RuntimeException("Customer not found with id: "+ customerId));
        if(vehicleRepository.existsByRegistrationNumber(vehicle.getRegistrationNumber())){
            throw new RuntimeException("Vehicle with this registration number already exists");
        }
        vehicle.setCustomer(customer);
        return vehicleRepository.save(vehicle);
    }

    // Get vehicle details
    public Vehicle getVehicleDetails(Integer vehicleId){
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(()-> new RuntimeException("Vehicle not found with id: "+ vehicleId));
    }

    // update vehicle details

    public Vehicle updateVehicleDetails(Integer vehicleId,Vehicle vehicleDetails){
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(()-> new RuntimeException("Vehicle not found with id: "+ vehicleId));
        vehicle.setRegistrationNumber(vehicleDetails.getRegistrationNumber());
        vehicle.setMake(vehicleDetails.getMake());
        vehicle.setModel(vehicleDetails.getModel());
        vehicle.setYearOfManufacture(vehicleDetails.getYearOfManufacture());
        vehicle.setVehicleType(vehicleDetails.getVehicleType());
        return vehicleRepository.save(vehicle);
    }

    //Get all vehicles for a customer
    public List<Vehicle> getVehiclesByCustomerId(Integer customerId){
        return vehicleRepository.findByCustomer_CustomerId(customerId);
    }

    //Get all Vehicles from DB
    public List<Vehicle> getAllVehicles(){
        return vehicleRepository.findAll();
    }
    //Delete Vehicle
    public void deleteVehicle(Integer vehicleId){
        if(!vehicleRepository.existsById(vehicleId)){
            throw new RuntimeException("Vehicle not found with this id: "+ vehicleId);
        }
        vehicleRepository.deleteById(vehicleId);
    }
}
