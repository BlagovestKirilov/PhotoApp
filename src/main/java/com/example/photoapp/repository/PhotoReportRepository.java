package com.example.photoapp.repository;

import com.example.photoapp.entity.PhotoReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoReportRepository extends JpaRepository<PhotoReport, Long> {
}
