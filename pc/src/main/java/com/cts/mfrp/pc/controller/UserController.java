package com.cts.mfrp.pc.controller;

import com.cts.mfrp.pc.dto.UserDTO;
import com.cts.mfrp.pc.model.User;
import com.cts.mfrp.pc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register/buyer")
    public ResponseEntity<User> registerBuyer(@RequestBody UserDTO userDTO) {
        User savedUser = userService.registerBuyer(userDTO);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }
}