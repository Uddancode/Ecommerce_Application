package com.ecommerce.project.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {

    private Long addressId;
    @NotBlank
    @Size(min = 5, message = "Street name must be at least 5 characters")
    private String street;
    @NotBlank
    @Size(min = 5, message = "BuildingName name must be at least 5 characters")
    private String buildingName;
    @NotBlank
    @Size(min = 2, message = "City name must be at least 2 characters")
    private String city;
    @NotBlank
    @Size(min = 2, message = "State name must be at least 2 characters")
    private String state;
    @NotBlank
    @Size(min = 2, message = "Country name must be at least 2 characters")
    private String country;
    @NotBlank
    @Size(min = 5, message = "Pin code name must be at least 5 characters")
    private String pincode;
}
