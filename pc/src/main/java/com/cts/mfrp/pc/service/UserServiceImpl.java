package com.cts.mfrp.pc.service;

import com.cts.mfrp.pc.dto.UserDTO;
import com.cts.mfrp.pc.model.User;
import com.cts.mfrp.pc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User registerBuyer(UserDTO userDTO) {
        User user = new User();

        // Mapping based on your User.java fields
        user.setName(userDTO.getFullName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhoneNumber());
        user.setPasswordHash(userDTO.getPassword()); // Maps to password_hash column
        user.setLat(userDTO.getLatitude());
        user.setLng(userDTO.getLongitude());
        user.setRole("BUYER");

        // id is UUID generated, createdAt is handled by @CreationTimestamp
        return userRepository.save(user);
    }
}