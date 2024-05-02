package com.example.photoapp.entity;

import com.example.photoapp.enums.CodeConfirmationEnum;
import com.example.photoapp.enums.CodeConfirmationStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserConfirmation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    String email;

    @Column(nullable = false)
    String confirmationCode;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    CodeConfirmationEnum codeConfirmation;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    CodeConfirmationStatusEnum codeConfirmationStatus;

    @Column(nullable = false)
    Date date = new Date();
}
