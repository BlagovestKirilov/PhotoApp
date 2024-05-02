package com.example.photoapp.repository;

import com.example.photoapp.entity.PhotoComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoCommentRepository extends JpaRepository<PhotoComment, Long> {
}
