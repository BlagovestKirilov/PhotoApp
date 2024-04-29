package com.example.photoapp.service;


import com.example.photoapp.entities.dto.ConfirmationDto;
import com.example.photoapp.entities.dto.LoginUserDto;
import com.example.photoapp.entities.dto.UserDto;
import com.example.photoapp.entities.User;
import com.example.photoapp.enums.RegistrationStatusEnum;

public interface UserService {
    User login(LoginUserDto loginUser);
    void logout(LoginUserDto loginUser);
    void saveUser(UserDto userDto);
    User findUserByEmail(String email);
    Boolean confirmUser(ConfirmationDto confirmationDto);
    void updateRegistrationStatus(String email, RegistrationStatusEnum registrationStatusEnum);
    void forgotPassword(String email);
}
