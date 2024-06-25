package com.example.photoapp.repository;

import com.example.photoapp.entity.Photo;
import com.example.photoapp.entity.PhotoReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoReportRepository extends JpaRepository<PhotoReport, Long> {
    List<PhotoReport> findAllByReportedPhoto(Photo reportedPhoto);
}
