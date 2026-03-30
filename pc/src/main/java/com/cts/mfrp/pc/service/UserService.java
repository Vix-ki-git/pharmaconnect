package com.cts.mfrp.pc.service;

import com.cts.mfrp.pc.dto.UserDTO;
import com.cts.mfrp.pc.model.User;

public interface UserService {
    User registerBuyer(UserDTO userDTO);
    String initiatePasswordReset(String email);
    void resetPassword(String token, String newPassword);
    void logout(String userId);
}