package com.example.photoapp.security.service;


import com.example.photoapp.security.dto.UserDto;
import com.example.photoapp.security.model.User;

public interface UserService {
    void saveUser(UserDto userDto);

    User findUserByEmail(String email);
}
