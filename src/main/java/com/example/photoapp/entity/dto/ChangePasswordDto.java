package com.example.photoapp.entity.dto;

import com.example.photoapp.enums.ChangePasswordEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordDto {

    private String oldPassword;

    private String newPassword;

    private String email;

    private ChangePasswordEnum changePasswordEnum;
}
