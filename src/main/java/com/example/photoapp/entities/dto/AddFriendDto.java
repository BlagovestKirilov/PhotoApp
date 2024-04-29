package com.example.photoapp.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddFriendDto {
    private String name;

    private String profilePhotoData;

    private String email;

    private Boolean isPendingStatus;
}
