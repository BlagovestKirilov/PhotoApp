package com.example.photoapp.service;


import com.example.photoapp.entity.Notification;
import com.example.photoapp.entity.dto.ConfirmationDto;
import com.example.photoapp.entity.dto.LoginUserDto;
import com.example.photoapp.entity.dto.UserBanDto;
import com.example.photoapp.entity.dto.UserDto;
import com.example.photoapp.entity.User;
import com.example.photoapp.enums.RegistrationStatusEnum;

import java.util.List;

public interface UserService {
    User login(LoginUserDto loginUser);
    void logout();
    void saveUser(UserDto userDto);
    User findUserByEmailForRegistration(String email);
    Boolean confirmUser(ConfirmationDto confirmationDto);
    void updateRegistrationStatus(String email, RegistrationStatusEnum registrationStatusEnum);
    void forgotPassword(String email);
    void banUser(UserBanDto userBanDto);
    Boolean isThereActiveBans();
    List<String> getCurrentUserNotification();
}
