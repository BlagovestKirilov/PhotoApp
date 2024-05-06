package com.example.photoapp.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PhotoReportDto {
    private PhotoDto photoDto;

    List<String> reportedReasons;
}
