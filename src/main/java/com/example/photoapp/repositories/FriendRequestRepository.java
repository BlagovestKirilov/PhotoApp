package com.example.photoapp.repositories;

import com.example.photoapp.entities.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findFriendRequestByReceiverIdAndStatus(Long receiverId, String status);
}
