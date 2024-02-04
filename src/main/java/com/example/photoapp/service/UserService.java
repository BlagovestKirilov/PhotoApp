package com.example.photoapp.service;


import com.example.photoapp.entities.dto.UserDto;
import com.example.photoapp.entities.User;

public interface UserService {
    void saveUser(UserDto userDto);

    User findUserByEmail(String email);
}
