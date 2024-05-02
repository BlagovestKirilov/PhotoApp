package com.example.photoapp.repository;

import com.example.photoapp.entity.PhotoReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoReportRepository extends JpaRepository<PhotoReport, Long> {
}
