package com.example.photoapp.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.example.photoapp.entities.FriendRequest;
import com.example.photoapp.entities.Photo;
import com.example.photoapp.entities.PhotoComment;
import com.example.photoapp.entities.User;
import com.example.photoapp.entities.dto.AddFriendDto;
import com.example.photoapp.entities.dto.PhotoDto;
import com.example.photoapp.enums.FriendRequestStatusEnum;
import com.example.photoapp.repositories.FriendRequestRepository;
import com.example.photoapp.repositories.PhotoCommentRepository;
import com.example.photoapp.repositories.PhotoRepository;
import com.example.photoapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PhotoService {
    @Value("${cloud.aws.bucketName}")
    private String BUCKET_NAME;

    @Autowired
    private PhotoRepository photoRepository;
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private PhotoCommentRepository photoCommentRepository;
    @Autowired
    private AmazonS3 s3Client;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    FriendRequestRepository friendRequestRepository;

    public void uploadToS3(File file) {
        String s3Key = file.getName();

        PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, s3Key, file);
        s3Client.putObject(putObjectRequest);
    }

    public void deleteFromS3(String fileName) {
        Photo photo = photoRepository.findByFileName(fileName);
        if (Objects.nonNull(photo)) {
            if (Objects.equals(photo.getUser().getId(), userService.currentUser.getId())) {
                s3Client.deleteObject(BUCKET_NAME, fileName);
                photoRepository.delete(photo);
            }
        }
    }

    public List<PhotoDto> getFromS3() {

        List<Photo> photosInDatabase = photoRepository.findAll().stream()
                .filter(photo -> {
                    long photoUploaderId = photo.getUser().getId();
                    return userService.currentUser.getFriends().stream()
                            .anyMatch(friend -> friend.getId() == photoUploaderId)
                            || userService.currentUser.getId() == photoUploaderId;
                })
                .toList();

        return photosInDatabase.stream().map(photo -> {
            ResponseEntity<byte[]> b = getImage(photo.getFileName());
            PhotoDto photoDto = new PhotoDto();
            photoDto.setData(Base64.getEncoder().encodeToString(b.getBody()));
            photoDto.setUserName(photo.getUser().getName());
            photoDto.setFileName(photo.getFileName());
            photoDto.setNumberLikes(photo.getLikedPhotoUsers().size());
            photoDto.setPhotoComments(photo.getPhotoComments());
            return photoDto;
        }).collect(Collectors.toList());
    }

    public ResponseEntity<byte[]> getImage(String key) {
        try {
            S3Object s3Object = s3Client.getObject(BUCKET_NAME, key);
            InputStream inputStream = s3Object.getObjectContent();
            byte[] bytes = IOUtils.toByteArray(inputStream);
            return ResponseEntity.ok().body(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public void save(File file) {
        Photo photo = new Photo();
        photo.setFileName(file.getName());
        photo.setUser(userService.currentUser);
        photoRepository.save(photo);
    }

    public void likePhoto(String fileName){
        Photo photo = photoRepository.findByFileName(fileName);
        photo.getLikedPhotoUsers().add(userService.currentUser);
        photoRepository.save(photo);
    }

    public void addComment(String fileName, String text){
        Photo photo = photoRepository.findByFileName(fileName);
        PhotoComment photoComment = new PhotoComment();
        photoComment.setText(text);
        photoComment.setCommentMaker(userService.currentUser);
        photoCommentRepository.save(photoComment);
        photo.getPhotoComments().add(photoComment);
        photoRepository.save(photo);
    }

    public List<AddFriendDto> findNonFriendUsers() {
        List<User> users = userRepository.findNonFriendUsersByUser(userService.currentUser.getEmail());
        List<User> pending = friendRequestRepository.findAllBySender(userService.currentUser)
                .stream()
                .filter(friendRequest -> friendRequest.getStatus() == FriendRequestStatusEnum.PENDING)
                .map(FriendRequest::getReceiver)
                .toList();
        List<User> rejectedOrAccepted = friendRequestRepository.findAllBySender(userService.currentUser)
                .stream()
                .filter(friendRequest -> friendRequest.getStatus() == FriendRequestStatusEnum.REJECTED || friendRequest.getStatus() == FriendRequestStatusEnum.ACCEPTED)
                .map(FriendRequest::getReceiver)
                .toList();
        return users.stream()
                .filter(user -> !rejectedOrAccepted.contains(user))
                .map(user -> {
                    AddFriendDto addFriendDto = new AddFriendDto();
                    addFriendDto.setName(user.getName());
                    addFriendDto.setEmail(user.getEmail());
                    ResponseEntity<byte[]> b = getImage(user.getProfilePhoto().getFileName());
                    addFriendDto.setProfilePhotoData(Base64.getEncoder().encodeToString(b.getBody()));
                    addFriendDto.setIsPendingStatus(pending.contains(user) ? Boolean.TRUE : Boolean.FALSE);
                    return addFriendDto;
                })
                .collect(Collectors.toList());
    }
}
