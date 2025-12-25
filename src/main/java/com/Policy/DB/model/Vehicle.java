package com.Policy.DB.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "Vehicle")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer vehicleId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customerId", nullable = false)
    private Customer customer;

    @Column(length = 20)
    private String registrationNumber;

    @Column(length = 50)
    private String make;

    @Column(length = 50)
    private String model;

    private Integer yearOfManufacture;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('CAR','BIKE','TRUCK')")
    private VehicleType vehicleType;

    @JsonIgnore
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Policy> policies;
}