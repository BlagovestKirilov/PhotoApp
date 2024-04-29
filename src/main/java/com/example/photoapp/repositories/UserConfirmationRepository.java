package com.example.photoapp.repositories;

import com.example.photoapp.entities.UserConfirmation;
import com.example.photoapp.enums.CodeConfirmationEnum;
import com.example.photoapp.enums.CodeConfirmationStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserConfirmationRepository extends JpaRepository<UserConfirmation, Long> {
    UserConfirmation findByEmail(String email);

    UserConfirmation findUserConfirmationByEmailAndCodeConfirmationAndCodeConfirmationStatus(String email, CodeConfirmationEnum codeConfirmation, CodeConfirmationStatusEnum codeConfirmationStatus);
}
