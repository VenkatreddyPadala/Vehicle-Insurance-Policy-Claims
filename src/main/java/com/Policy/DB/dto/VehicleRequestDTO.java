package com.Policy.DB.dto;

import com.Policy.DB.model.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRequestDTO {
    private String registrationNumber;
    private String make;
    private String model;
    private Integer yearOfManufacture;
    private VehicleType vehicleType;
}