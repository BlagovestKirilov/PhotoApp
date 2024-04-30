package com.example.photoapp.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.example.photoapp.entities.FriendRequest;
import com.example.photoapp.entities.Photo;
import com.example.photoapp.entities.PhotoComment;
import com.example.photoapp.entities.User;
import com.example.photoapp.entities.dto.FriendDto;
import com.example.photoapp.entities.dto.PhotoDto;
import com.example.photoapp.entities.dto.UserDto;
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

        List<Photo> photosInDatabase = photoRepository.findByUserIsNotNull().stream()
                .filter(photo -> {
                    String photoUploaderEmail = photo.getUser().getEmail();
                    return userService.currentUser.getFriendList().stream()
                            .anyMatch(friend -> friend.getEmail().equals(photoUploaderEmail))
                            || userService.currentUser.getEmail().equals(photoUploaderEmail);
                })
                .toList();

        return photosInDatabase.stream().map(photo -> {
            ResponseEntity<byte[]> picture = getImage(photo.getFileName());
            PhotoDto photoDto = new PhotoDto();
            photoDto.setData(Base64.getEncoder().encodeToString(picture.getBody()));
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

    public void changeProfilePicture(File file) {
        Photo photo = new Photo();
        photo.setFileName(file.getName());
        photo.setUser(userService.currentUser);
        userService.currentUser.setProfilePhoto(photo);
        photoRepository.save(photo);
        userRepository.save(userService.currentUser);
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

    public List<FriendDto> findNonFriendUsers() {
        List<User> users = userRepository.findNonFriendUsersByUser(userService.currentUser.getEmail());
        List<User> pending = friendRequestRepository.findAllBySenderAndStatus(userService.currentUser,FriendRequestStatusEnum.PENDING)
                .stream()
                .map(FriendRequest::getReceiver)
                .toList();
        List<User> pendingFromCurrentUser = friendRequestRepository.findAllByReceiverAndStatus(userService.currentUser,FriendRequestStatusEnum.PENDING)
                .stream()
                .map(FriendRequest::getSender)
                .toList();
        return users.stream()
                .map(user -> {
                    List<User> g= friendRequestRepository.findAllBySender(user)
                            .stream().filter(friendRequest -> friendRequest.getStatus() == FriendRequestStatusEnum.PENDING)
                            .map(FriendRequest::getReceiver).toList();
                    FriendDto friendDto = new FriendDto();
                    friendDto.setName(user.getName());
                    friendDto.setEmail(user.getEmail());
                    ResponseEntity<byte[]> b = getImage(user.getProfilePhoto().getFileName());
                    friendDto.setProfilePhotoData(Base64.getEncoder().encodeToString(b.getBody()));
                    friendDto.setIsPendingStatus(pending.contains(user) ? Boolean.TRUE : Boolean.FALSE);
                    friendDto.setIsPendingStatusFromCurrentUser(pendingFromCurrentUser.contains(user) ? Boolean.TRUE : Boolean.FALSE);
                    return friendDto;
                })
                .collect(Collectors.toList());
    }
    public List<FriendDto> getCurrentUserFriends(){
        return userService.currentUser.getFriendList().stream()
                .map(user -> {
                    FriendDto friendDto = new FriendDto();
                    friendDto.setName(user.getName());
                    friendDto.setEmail(user.getEmail());
                    ResponseEntity<byte[]> b = getImage(user.getProfilePhoto().getFileName());
                    friendDto.setProfilePhotoData(Base64.getEncoder().encodeToString(b.getBody()));
                    return friendDto;
                })
                .collect(Collectors.toList());
    }

    public List<FriendDto> getFriendRequests() {
        List<FriendRequest> foundFriendRequest = friendRequestRepository.findAllByReceiverAndStatus(userService.currentUser, FriendRequestStatusEnum.PENDING);
//        List<FriendRequestDto> resultList = new ArrayList<>();
//        for (FriendRequest friendRequest : foundFriendRequest) {
//            String senderName = friendRequest.getSender().getName();
//            resultList.add(new FriendRequestDto(friendRequest.getId(), senderName));
//        }
//        return resultList;
        return foundFriendRequest.stream()
                .map(friendRequest -> {
                    User sender = friendRequest.getSender();
                    FriendDto friendDto = new FriendDto();
                    friendDto.setName(sender.getName());
                    friendDto.setEmail(sender.getEmail());
                    ResponseEntity<byte[]> b = getImage(sender.getProfilePhoto().getFileName());
                    friendDto.setProfilePhotoData(Base64.getEncoder().encodeToString(b.getBody()));
                    friendDto.setIsPendingStatus(Boolean.TRUE);
                    return friendDto;
                })
                .collect(Collectors.toList());
    }

    public UserDto getCurrentUserDto(){
        UserDto userDto = new UserDto();
        userDto.setName(userService.currentUser.getName());
        userDto.setEmail(userService.currentUser.getEmail());
        ResponseEntity<byte[]> picture = getImage(userService.currentUser.getProfilePhoto().getFileName());
        userDto.setProfilePictureData(Base64.getEncoder().encodeToString(picture.getBody()));
        return userDto;
    }
}
