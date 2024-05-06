package com.example.photoapp.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.example.photoapp.entity.*;
import com.example.photoapp.entity.dto.*;
import com.example.photoapp.enums.FriendRequestStatusEnum;
import com.example.photoapp.enums.PhotoReportReasonEnum;
import com.example.photoapp.enums.RoleEnum;
import com.example.photoapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PhotoServiceImpl {
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
    @Autowired
    PhotoReportRepository photoReportRepository;

    public void uploadToS3(File file) {
        String s3Key = file.getName();

        PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, s3Key, file);
        s3Client.putObject(putObjectRequest);
    }

    public void deleteFromS3(String photoFileName) {
        Photo photo = photoRepository.findByFileName(photoFileName);
        if (Objects.nonNull(photo)) {
            if (Objects.equals(photo.getUser().getId(), userService.currentUser.getId()) || userService.currentUser.getRole().getName() == RoleEnum.ROLE_ADMIN) {
                //s3Client.deleteObject(BUCKET_NAME, photoFileName);
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

        return getPhotoDtos(photosInDatabase);
    }
    public List<PhotoDto> getAllPhoto() {
        return getPhotoDtos(photoRepository.findByUserIsNotNull());
    }
    public List<PhotoReportDto> getReportedPhotos(){
        List<PhotoReport> photoReports = photoReportRepository.findAll();
        Map<Photo, List<PhotoReportReasonEnum>> g = photoReportRepository.findAll().stream()
                // Sort the PhotoReports by reported photo ID
                .sorted(Comparator.comparing(photoReport -> photoReport.getReportedPhoto().getId()))
                // Group the PhotoReports by reported photo
                .collect(Collectors.groupingBy(
                        PhotoReport::getReportedPhoto,
                        Collectors.mapping(PhotoReport::getReportReason, Collectors.toList())
                ));
        return photoReportRepository.findAll().stream()
                .sorted(Comparator.comparing(photoReport -> photoReport.getReportedPhoto().getId()))
                .collect(Collectors.groupingBy(
                        PhotoReport::getReportedPhoto,
                        Collectors.mapping(PhotoReport::getReportReason, Collectors.toList())
                )).keySet().stream().map(photo -> {
                    PhotoReportDto photoReportDto = new PhotoReportDto();
                    ResponseEntity<byte[]> picture = getImage(photo.getFileName());
                    PhotoDto photoDto = new PhotoDto();
                    photoDto.setData(Base64.getEncoder().encodeToString(picture.getBody()));
                    photoDto.setUserName(photo.getUser().getName());
                    photoDto.setFileName(photo.getFileName());
                    photoDto.setNumberLikes(photo.getLikedPhotoUsers().size());
                    photoDto.setPhotoComments(getPhotoCommentsDtos(photo));
                    ResponseEntity<byte[]> profilePicture = getImage(photo.getUser().getProfilePhoto().getFileName());
                    photoDto.setUserProfilePhotoData(Base64.getEncoder().encodeToString(profilePicture.getBody()));
                    photoDto.setStatus(photo.getStatus());
                    photoReportDto.setPhotoDto(photoDto);
                    photoReportDto.setReportedReasons(g.get(photo).stream().map(Enum::name).collect(Collectors.toList()));
                    return photoReportDto;
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

    public void save(File file, String status) {
        Photo photo = new Photo();
        photo.setFileName(file.getName());
        photo.setUser(userService.currentUser);
        photo.setStatus(status);
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
        photoComment.setPhoto(photo);
        photoCommentRepository.save(photoComment);
        //photo.getPhotoComments().add(photoComment);
        //photoRepository.save(photo);
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
//                    List<User> g= friendRequestRepository.findAllBySender(user)
//                            .stream().filter(friendRequest -> friendRequest.getStatus() == FriendRequestStatusEnum.PENDING)
//                            .map(FriendRequest::getReceiver).toList();
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

    public List<FriendDto> getAllUsers(){
        return userRepository.findAll().stream()
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
        userDto.setRole(userService.currentUser.getRole().getName().toString());
        ResponseEntity<byte[]> picture = getImage(userService.currentUser.getProfilePhoto().getFileName());
        userDto.setProfilePictureData(Base64.getEncoder().encodeToString(picture.getBody()));
        return userDto;
    }

    public List<PhotoDto> getCurrentUserPhotos(){
        List<Photo> currentUserPhotos = photoRepository.findByUser(userService.currentUser);
        return getPhotoDtos(currentUserPhotos);
    }

    private List<PhotoDto> getPhotoDtos(List<Photo> currentUserPhotos) {
        return currentUserPhotos.stream().map(photo -> {
            ResponseEntity<byte[]> picture = getImage(photo.getFileName());
            PhotoDto photoDto = new PhotoDto();
            photoDto.setData(Base64.getEncoder().encodeToString(picture.getBody()));
            photoDto.setUserName(photo.getUser().getName());
            photoDto.setFileName(photo.getFileName());
            photoDto.setNumberLikes(photo.getLikedPhotoUsers().size());
            photoDto.setPhotoComments(getPhotoCommentsDtos(photo));
            ResponseEntity<byte[]> profilePicture = getImage(photo.getUser().getProfilePhoto().getFileName());
            photoDto.setUserProfilePhotoData(Base64.getEncoder().encodeToString(profilePicture.getBody()));
            photoDto.setStatus(photo.getStatus());
            return photoDto;
        }).collect(Collectors.toList());
    }

    private List<PhotoCommentDto> getPhotoCommentsDtos(Photo photo){
        return photoCommentRepository.findAllByPhoto(photo).stream().map(photoComment -> {
            PhotoCommentDto photoCommentDto= new PhotoCommentDto();
            photoCommentDto.setCommentMaker(photoComment.getCommentMaker().getName());
            photoCommentDto.setText(photoComment.getText());
            return photoCommentDto;
        }).collect(Collectors.toList());
    }

    public void reportPhoto(String photoName, String reason){
        PhotoReport photoReport = new PhotoReport();
        photoReport.setReporterUser(userService.currentUser);
        photoReport.setReportedPhoto(photoRepository.findByFileName(photoName));
        photoReport.setReportReason(PhotoReportReasonEnum.valueOf(reason));
        photoReportRepository.save(photoReport);
    }
}
