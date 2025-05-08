package com.ecommerce.project.controller;

import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.APIResponse;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.service.AddressService;
import com.ecommerce.project.utils.AuthUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class AddressController {


    // for logged in users
    @Autowired
    private AddressService addressService;

    @Autowired
    AuthUtil authUtil;


    @PostMapping("/addresses")
    public ResponseEntity<AddressDTO> createAddress(@Valid @RequestBody AddressDTO addressDTO) {

        log.info("Request received : Create address for this details : {}", addressDTO);

        User user = authUtil.loggedInUser();
        AddressDTO savedAddressDTO = addressService.createAddress(addressDTO, user);

        log.info("Address added successfully ,{}", savedAddressDTO);

        return new ResponseEntity<>(savedAddressDTO, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDTO>> getAddresses() {

        log.info("Request received : get addresses ");

        List<AddressDTO> addressDTOList = addressService.getAllAddresses();

        log.info("All the address fetched successfully , {}", addressDTOList.size());
        return new ResponseEntity<>(addressDTOList, HttpStatus.OK);
    }

    @GetMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long addressId) {

        log.info("Request received : get address by Id :{}", addressId);

        AddressDTO addressDTO = addressService.getAddressById(addressId);

        log.info("Address fetched successfully , {}", addressDTO);
        return new ResponseEntity<>(addressDTO, HttpStatus.OK);
    }

    @GetMapping("/users/addresses")
    public ResponseEntity<List<AddressDTO>> getUserAddresses() {
        User user = authUtil.loggedInUser();

        log.info("Request received :  get user addresses {}", user.getUserName());
        List<AddressDTO> addressDTOList = addressService.getUserAddress(user);

        log.info("Address fetched successfully, {}", addressDTOList.size());
        return new ResponseEntity<>(addressDTOList, HttpStatus.OK);

//        return new ResponseEntity<>(addressService.getUserAddress(authUtil.loggedInUser()), HttpStatus.OK);
    }

    @PutMapping("/addresses/{addressId}")

    public ResponseEntity<AddressDTO> updateAddress(@Valid @PathVariable Long addressId, @RequestBody AddressDTO addressDTO) {

        log.info("Request received : Update address by address Id : {}, {}", addressId, addressDTO);
        AddressDTO updateAddressDTO = addressService.updateAddress(addressId, addressDTO);

        log.info("Address updated successfully {}", updateAddressDTO);
        return new ResponseEntity<>(updateAddressDTO, HttpStatus.OK);
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<APIResponse> deleteAddress(@PathVariable Long addressId) {

        log.info("Request received : Delete address by Address Id : {}", addressId);
        String status = addressService.deleteAddress(addressId);

        log.info("Address deleted successfully , {}", status);

        return new ResponseEntity<>(new APIResponse(status, true), HttpStatus.OK);

    }
}
