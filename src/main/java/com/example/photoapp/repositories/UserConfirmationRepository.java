package com.example.photoapp.repositories;

import com.example.photoapp.entities.UserConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserConfirmationRepository extends JpaRepository<UserConfirmation, Long> {
    UserConfirmation findByEmail(String email);
}
