package com.example.photoapp.repository;

import com.example.photoapp.entity.User;
import com.example.photoapp.entity.UserBan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface UserBanRepository extends JpaRepository<UserBan, Long> {
    List<UserBan> findAllByUser(User user);
}
