package com.example.photoapp.entities;

import com.example.photoapp.enums.FriendRequestStatusEnum;
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
@Table(name = "friend_request")
public class FriendRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    Long senderId;

    @Column(nullable = false)
    Long receiverId;

    @Column(nullable = false)
    String status = FriendRequestStatusEnum.PENDING.toString();

    @Column(nullable = false)
    Date sendRequestTime = new Date();
}
