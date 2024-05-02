package com.example.photoapp.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FriendDto {
    private String name;

    private String profilePhotoData;

    private String email;

    private Boolean isPendingStatus;

    private Boolean isPendingStatusFromCurrentUser;
}
