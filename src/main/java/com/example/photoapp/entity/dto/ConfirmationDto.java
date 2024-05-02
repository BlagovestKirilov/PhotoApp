package com.example.photoapp.entity.dto;

import com.example.photoapp.enums.CodeConfirmationEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmationDto {
    private String email;

    private String confirmationCode;

    private CodeConfirmationEnum codeConfirmationEnum;
}
