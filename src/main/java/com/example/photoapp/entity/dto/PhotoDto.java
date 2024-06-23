package com.example.photoapp.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
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

    private List<PhotoCommentDto> photoComments;

    private String userProfilePhotoData;

    private String status;

    private List<String> likeUsersEmails;

    private Date dateUploaded;

    private Boolean isPagePhoto;

    private String userEmail;
}
