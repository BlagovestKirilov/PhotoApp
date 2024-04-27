package com.example.photoapp.entities.dto;

import com.example.photoapp.entities.PhotoComment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PhotoDto {
    private String data;

    private String userName;

    private Integer numberLikes;

    private String fileName;

    private List<PhotoComment> photoComments;
}
