package com.example.photoapp.repository;

import com.example.photoapp.entity.Notification;
import com.example.photoapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByUserOrderByDateDesc(User user);
}
