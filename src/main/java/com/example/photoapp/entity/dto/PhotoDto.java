package com.example.photoapp.entity.dto;

import com.example.photoapp.entity.PhotoComment;
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

    private String userProfilePhotoData;

    private String status;
}
