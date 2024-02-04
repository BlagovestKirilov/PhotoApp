package com.example.photoapp.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FriendRequestDto {
    private Long friendRequestId;

    private String senderName;
}
