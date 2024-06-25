package com.example.photoapp.repository;

import com.example.photoapp.entity.Page;
import com.example.photoapp.entity.Photo;
import com.example.photoapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    Photo findByFileName(String name);
    Photo findByFileNameEndsWith(String name);
    List<Photo> findByUserIsNotNull();
    List<Photo> findByUserOrderByDateUploadedDesc(User user);
    List<Photo> findByPageOrderByDateUploadedDesc(Page page);
    List<Photo> findAllByPage(Page page);
    Photo findFirstByFileNameContains(String name);
}
