package com.example.photoapp.service;


import com.example.photoapp.entity.Notification;
import com.example.photoapp.entity.Page;
import com.example.photoapp.entity.dto.*;
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
    void editProfile(UserDto userDto);

    void savePage(String pageName);
    List<Page> getCurrentUserPage();
    List<Page> getAllPages();
    void likePage(String pageName, String userEmail);

    void unlikePage(String pageName, String userEmail);

    void editPage(PageDto pageDto);
}
