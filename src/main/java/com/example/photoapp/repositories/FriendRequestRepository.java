package com.example.photoapp.repositories;

import com.example.photoapp.entities.FriendRequest;
import com.example.photoapp.entities.User;
import com.example.photoapp.enums.FriendRequestStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findFriendRequestByReceiverIdAndStatus(Long receiverId, FriendRequestStatusEnum status);
    List<FriendRequest> findAllBySender(User sender);
}
