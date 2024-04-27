package com.example.photoapp.repositories;

import com.example.photoapp.entities.PhotoComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoCommentRepository extends JpaRepository<PhotoComment, Long> {
}
