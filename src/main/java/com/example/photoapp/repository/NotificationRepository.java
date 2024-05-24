package com.example.photoapp.repository;

import com.example.photoapp.entity.Notification;
import com.example.photoapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByUser(User user);
}
