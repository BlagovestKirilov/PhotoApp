package com.example.photoapp.repository;

import com.example.photoapp.entity.Photo;
import com.example.photoapp.entity.PhotoComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PhotoCommentRepository extends JpaRepository<PhotoComment, Long> {
    List<PhotoComment> findAllByPhoto(Photo photo);
}
