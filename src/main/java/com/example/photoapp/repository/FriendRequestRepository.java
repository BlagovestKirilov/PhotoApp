package com.example.photoapp.repository;

import com.example.photoapp.entity.FriendRequest;
import com.example.photoapp.entity.User;
import com.example.photoapp.enums.FriendRequestStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findFriendRequestByReceiverIdAndStatus(Long receiverId, FriendRequestStatusEnum status);
    List<FriendRequest> findAllBySender(User sender);
    List<FriendRequest> findAllBySenderAndStatus(User sender, FriendRequestStatusEnum status);
    List<FriendRequest> findAllByReceiverAndStatus(User receiver, FriendRequestStatusEnum status);

    Optional<FriendRequest> findFirstBySenderAndReceiverAndStatus(User sender, User receiver, FriendRequestStatusEnum status);
}
