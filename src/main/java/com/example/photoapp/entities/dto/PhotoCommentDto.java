package com.example.photoapp.entities.dto;

import com.example.photoapp.entities.User;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhotoCommentDto {
    private String text;

    private String commentMaker;
}
