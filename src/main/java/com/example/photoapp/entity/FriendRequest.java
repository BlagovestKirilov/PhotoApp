package com.example.photoapp.entity;

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
public class FriendRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(nullable = false)
    @ManyToOne
    User sender;

    @JoinColumn(nullable = false)
    @ManyToOne
    User receiver;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    FriendRequestStatusEnum status = FriendRequestStatusEnum.PENDING;

    @Column(nullable = false)
    Date sendRequestTime = new Date();
}
