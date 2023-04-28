package com.example.photoapp.repositories;

import com.example.photoapp.entities.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    Photo findByFileName(String name);
}
