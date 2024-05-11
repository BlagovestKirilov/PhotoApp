package com.example.photoapp.repository;

import com.example.photoapp.entity.User;
import com.example.photoapp.entity.UserBan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBanRepository extends JpaRepository<UserBan, Long> {
    List<UserBan> findAllByUser(User user);
}
