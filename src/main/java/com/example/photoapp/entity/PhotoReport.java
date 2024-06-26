package com.example.photoapp.entity;

import com.example.photoapp.enums.ReportReasonEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PhotoReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Photo reportedPhoto;

    @OneToOne
    private User reporterUser;

    @Enumerated(EnumType.STRING)
    ReportReasonEnum reportReason;
}
