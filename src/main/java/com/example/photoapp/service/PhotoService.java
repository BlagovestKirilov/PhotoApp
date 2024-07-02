package com.example.photoapp.service;

import com.example.photoapp.entity.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface PhotoService {

    void uploadToS3(File file);

    void deleteFromS3(String photoFileName);

    List<PhotoDto> getFromS3();

    List<PhotoDto> getAllPhoto();

    List<PhotoReportDto> getReportedPhotos();

    ResponseEntity<byte[]> getImage(String key);

    void save(File file, String status);

    void changeProfilePicture(File file);

    void changeProfilePicturePage(File file, String status, String pageName);

    void likePhoto(String fileName);

    void unLikePhoto(String fileName);

    void addComment(String fileName, String text);

    List<FriendDto> findNonFriendUsers();

    List<PageDto> getCurrentUserNowOwnedPages();

    List<FriendDto> getCurrentUserFriends();

    List<FriendDto> getCurrentUserNonFrinedsByCountry();

    List<FriendDto> getAllUsers();

    List<FriendDto> getFriendRequests();

    UserDto getCurrentUserDto();

    PageDto getPageByName(String name);

    List<PageDto> getPagesYouMayLike();

    List<PageDto> getCurrentUserPages();

    List<PageDto> getAllPagesPages();

    UserDto getUserDtoByEmail(String email);

    List<PhotoDto> getCurrentUserPhotos();

    List<PhotoDto> getUserPhotosByEmail(String email);

    List<PhotoDto> getPagePhotosByPageName(String pageName);

    void reportPhoto(String photoName, String reason);

    void uploadPhotoByPage(MultipartFile file, String status, String pageName) throws IOException;
}

