package com.example.photoapp.service.impl;


import com.example.photoapp.entities.User;
import com.example.photoapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    public User currentUser;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {

        currentUser = userRepository.findByEmail(usernameOrEmail);
        if (currentUser != null) {
            return new org.springframework.security.core.userdetails.User(currentUser.getEmail()
                    , currentUser.getPassword(),
                    currentUser.getRoles().stream()
                            .map((role) -> new SimpleGrantedAuthority(role.getName()))
                            .collect(Collectors.toList()));
        } else {
            throw new UsernameNotFoundException("Invalid email or password");
        }
    }
}
