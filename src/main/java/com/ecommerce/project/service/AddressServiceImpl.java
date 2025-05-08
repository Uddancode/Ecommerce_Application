package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repositories.AddressRepository;
import com.ecommerce.project.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AddressServiceImpl implements AddressService {


    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {

        log.info("Attempting to create address for user {} and address details ", user.getUserName(), addressDTO);


        Address address = modelMapper.map(addressDTO, Address.class);
        address.setUser(user);
        List<Address> addressList = user.getAddresses();
        addressList.add(address);
        user.setAddresses(addressList);
        Address savedAddress = addressRepository.save(address);
        // check

        log.info("Address saved for this user {}, {}", user.getUserName(), savedAddress);

        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAllAddresses() {

        log.info("Attempting to fetch all addresses from database ");
        List<Address> addressList = addressRepository.findAll();


        if (addressList.isEmpty()) {

            log.warn("No address found in the database");
            throw new APIException("No address found in the database");
        }

        log.info("Returning all the addresses {}", addressList.size());
        return addressList.stream().map(address -> modelMapper.map(address, AddressDTO.class)
        ).toList();

    }

    @Override
    public AddressDTO getAddressById(Long addressId) {

        log.info("Attempting to fetch address by Address Id {}", addressId);
        Address address = addressRepository.findById(addressId)
                .orElseThrow(
                        () -> {

                            log.warn("Address not found with this address Id {}", addressId);
                            return new ResourceNotFoundException("Address", "addressId", addressId);
                        });

        log.info("Returning the address for address id , {}, {}", addressId, address);
        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getUserAddress(User user) {


        log.info("Attempting to fetch all the addresses of User {}", user.getUserName());
        List<Address> addresses = user.getAddresses();

        if (addresses.isEmpty()) {
            log.warn("No address found for this user " + user.getUserName());
            throw new APIException("No address found for this user " + user.getUserName());
        }

        log.info("Returning the address for this user {} {}", user.getUserName(), addresses);
        return addresses.stream().map(address ->
                        modelMapper.map(address, AddressDTO.class))
                .toList();
    }

    @Override
    public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO) {

        log.info("Attempting to update User address by address Id {} with this {}", addressId, addressDTO);

        Address existingAddress = addressRepository.findById(addressId)
                .orElseThrow(
                        () -> {

                            log.warn("No address found with this address Id {}", addressId);
                            return new ResourceNotFoundException("Address", "addressId", addressId);
                        });

        existingAddress.setBuildingName(addressDTO.getBuildingName());
        existingAddress.setStreet(addressDTO.getStreet());
        existingAddress.setCity(addressDTO.getCity());
        existingAddress.setState(addressDTO.getState());
        existingAddress.setPincode(addressDTO.getPincode());
        existingAddress.setCountry(addressDTO.getCountry());

        Address updatedAddress = addressRepository.save(existingAddress);

        log.info("Address updated successfully {}", updatedAddress);

        User user = existingAddress.getUser();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        user.getAddresses().add(updatedAddress);
        userRepository.save(user);

        log.info("Updated address saved into User side {}", user.getUserName());

        return modelMapper.map(updatedAddress, AddressDTO.class);
    }

    @Override
    public String deleteAddress(Long addressId) {
        log.info("Attempting to delete the address By address Id {}", addressId);

        Address existingAddress = addressRepository.findById(addressId)
                .orElseThrow(
                        () -> {
                            log.warn("No address found for this address Id {}", addressId);
                            return new ResourceNotFoundException("Address", "addressId", addressId);
                        });

        User user = existingAddress.getUser();


        user.getAddresses().removeIf(address -> {

                    log.info("Address removed from user side");
                    return address.getAddressId().equals(addressId);
                }

        );
        userRepository.save(user);
        log.info("user saved after removing address from Address List ");


        return "Address deleted successfully with address Id : " + addressId;
    }
}
