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
public class PageDto {
    private String name;

    private String newPageName;

    private String profilePhotoData;

    private String ownerEmail;

    private String description;

    private String website;

    private List<String> likeUsersEmails;
}
