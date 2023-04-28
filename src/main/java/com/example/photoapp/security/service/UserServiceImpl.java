package com.example.photoapp.security.service;



import com.example.photoapp.entities.Photo;
import com.example.photoapp.security.dto.UserDto;
import com.example.photoapp.security.model.Role;
import com.example.photoapp.security.model.User;
import com.example.photoapp.security.repository.RoleRepository;
import com.example.photoapp.security.repository.UserRepository;
import com.example.photoapp.security.util.TbConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    public void saveUser(UserDto userDto) {
        Role role = roleRepository.findByName(TbConstants.Roles.USER);
        List<User> friends = new ArrayList<>();
        List<Photo> photos = new ArrayList<>();

        if (role == null)
            role = roleRepository.save(new Role(TbConstants.Roles.USER));

        User user = new User(userDto.getName(), userDto.getEmail(), passwordEncoder.encode(userDto.getPassword()),
                List.of(role),photos,friends);
        userRepository.save(user);

    }
    public void addFriend(String name){
        User foundUser = userRepository.findByName(name);
        User currentUser = customUserDetailsService.currentUser;
        //foundUser.getFriends().add(currentUser);
        currentUser.getFriends().add(foundUser);
        //userRepository.save(foundUser);
        userRepository.save(currentUser);
    }
    public void removeFriend(String name){
        User foundUser = userRepository.findByName(name);
        User currentUser = customUserDetailsService.currentUser;

        List<User> friendsList = currentUser.getFriends();
        friendsList.stream()
                .filter(friend -> friend.getName().equals(foundUser.getName()))
                .findFirst()
                .ifPresent(friendsList::remove);
        userRepository.save(currentUser);

    }
    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
