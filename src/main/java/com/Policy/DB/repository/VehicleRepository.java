package com.Policy.DB.repository;

import com.Policy.DB.model.Vehicle;
import com.Policy.DB.model.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle,Integer> {
    List<Vehicle> findByCustomer_CustomerId(Integer customerId);
    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);
    List<Vehicle> findByVehicleType(VehicleType vehicleType);
    List<Vehicle> findByMakeAndModel(String make, String model);
    boolean existsByRegistrationNumber(String registrationNumber);
}
