package com.example.photoapp.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.example.photoapp.entity.*;
import com.example.photoapp.entity.dto.*;
import com.example.photoapp.enums.FriendRequestStatusEnum;
import com.example.photoapp.enums.ReportReasonEnum;
import com.example.photoapp.enums.RoleEnum;
import com.example.photoapp.repository.*;
import com.example.photoapp.util.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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
    @Autowired
    PageRepository pageRepository;
    @Autowired
    CurrentUser currentUser;

    public void uploadToS3(File file) {
        String s3Key = file.getName();

        PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, s3Key, file);
        s3Client.putObject(putObjectRequest);
    }

    public void deleteFromS3(String photoFileName) {
        Photo photo = photoRepository.findByFileName(photoFileName);
        if (Objects.nonNull(photo)) {
            if (Objects.equals(photo.getUser().getId(), userService.currentUser.getUser().getId()) || userService.currentUser.getUser().getRole().getName() == RoleEnum.ROLE_ADMIN) {
                //s3Client.deleteObject(BUCKET_NAME, photoFileName);
                photoRepository.delete(photo);
                if(userService.currentUser.getUser().getRole().getName() == RoleEnum.ROLE_ADMIN){
                    userService.generateNotification(photo.getUser(), "Admin has deleted your photo!");
                }
            }
        }
    }

    public List<PhotoDto> getFromS3() {

        List<Photo> photosInDatabaseUser = new ArrayList<>(photoRepository.findByUserIsNotNull().stream()
                .filter(photo -> {
                    String photoUploaderEmail = photo.getUser().getEmail();
                    return userService.currentUser.getUser().getFriendList().stream()
                            .anyMatch(friend -> friend.getEmail().equals(photoUploaderEmail))
                            || userService.currentUser.getUser().getEmail().equals(photoUploaderEmail);
                })
                .toList());
        List<Photo> photosInDatabasePage = new ArrayList<>();
        pageRepository.findAllByLikedPageUsersContains(userService.currentUser.getUser()).forEach(page -> photosInDatabasePage.addAll(photoRepository.findAllByPage(page)));
        List<Page> pages = pageRepository.findAllByLikedPageUsersContains(userService.currentUser.getUser());
        List<Photo> p1 = pages.stream()
                .flatMap(page -> photoRepository.findAllByPage(page).stream())
                .toList();
        photosInDatabasePage.addAll(p1);
        List<Page> pages1 = pageRepository.findAllByLikedPageUsersNotContainsAndIsPagePublic(userService.currentUser.getUser(), Boolean.TRUE);
        List<Photo> p12 = pages.stream()
                .flatMap(page -> photoRepository.findAllByPage(page).stream())
                .toList();
        photosInDatabasePage.addAll(p12);
        List<PhotoDto> photoDtos = getPhotoDtos(photosInDatabaseUser, false);
        photoDtos.addAll(getPhotoDtos(photosInDatabasePage, true));
        photoDtos.sort(Comparator.comparing(PhotoDto::getDateUploaded).reversed());
        return photoDtos;
    }

    public List<PhotoDto> getAllPhoto() {
        return getPhotoDtos(photoRepository.findByUserIsNotNull(), false);
    }
    public List<PhotoReportDto> getReportedPhotos(){
        Map<Photo, List<ReportReasonEnum>> reportMap = photoReportRepository.findAll().stream()
                .sorted(Comparator.comparing(photoReport -> photoReport.getReportedPhoto().getId()))
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
                    photoDto.setUserName(photo.getUser() != null ? photo.getUser().getName() : photo.getPage().getPageName());
                    photoDto.setFileName(photo.getFileName());
                    photoDto.setNumberLikes(photo.getLikedPhotoUsers().size());
                    photoDto.setPhotoComments(getPhotoCommentsDtos(photo));
                    ResponseEntity<byte[]> profilePicture = getImage(photo.getUser() != null ? photo.getUser().getProfilePhoto().getFileName() : photo.getPage().getProfilePhoto().getFileName());
                    photoDto.setUserProfilePhotoData(Base64.getEncoder().encodeToString(profilePicture.getBody()));
                    photoDto.setStatus(photo.getStatus());
                    photoReportDto.setPhotoDto(photoDto);
                    photoReportDto.setReportedReasons(reportMap.get(photo).stream().map(Enum::name).collect(Collectors.toList()));
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
        photo.setUser(userService.currentUser.getUser());
        photo.setStatus(status);
        photoRepository.save(photo);
    }

    public void changeProfilePicture(File file) {
        Photo photo = new Photo();
        photo.setFileName(file.getName());
        photo.setUser(userService.currentUser.getUser());
        userService.currentUser.getUser().setProfilePhoto(photo);
        photoRepository.save(photo);
        userRepository.save(userService.currentUser.getUser());
    }

    public void likePhoto(String fileName){
        Photo photo = photoRepository.findByFileName(fileName);
        photo.getLikedPhotoUsers().add(userService.currentUser.getUser());
        userService.generateNotification(photo.getUser(), userService.currentUser.getUser().getName() +" liked your photo!");
        photoRepository.save(photo);
    }
    public void unLikePhoto(String fileName){
        Photo photo = photoRepository.findByFileName(fileName);
        List<User> users = photo.getLikedPhotoUsers().stream().filter(user -> !user.getEmail().equals(userService.currentUser.getUser().getEmail())).toList();
        photo.setLikedPhotoUsers(users);
        userService.generateNotification(photo.getUser(), userService.currentUser.getUser().getName() +" unliked your photo!");
        photoRepository.save(photo);
    }


    public void addComment(String fileName, String text){
        Photo photo = photoRepository.findByFileName(fileName);
        PhotoComment photoComment = new PhotoComment();
        photoComment.setText(text);
        photoComment.setCommentMaker(userService.currentUser.getUser());
        photoComment.setPhoto(photo);
        photoCommentRepository.save(photoComment);
        //photo.getPhotoComments().add(photoComment);
        //photoRepository.save(photo);
    }

    public List<FriendDto> findNonFriendUsers() {
        List<User> users = userRepository.findNonFriendUsersByUser(userService.currentUser.getUser().getEmail());
        List<User> pending = friendRequestRepository.findAllBySenderAndStatus(userService.currentUser.getUser(),FriendRequestStatusEnum.PENDING)
                .stream()
                .map(FriendRequest::getReceiver)
                .toList();
        List<User> pendingFromCurrentUser = friendRequestRepository.findAllByReceiverAndStatus(userService.currentUser.getUser(),FriendRequestStatusEnum.PENDING)
                .stream()
                .map(FriendRequest::getSender)
                .toList();
        return users.stream()
                .map(user -> {
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
    public List<PageDto> getCurrentUserNowOwnedPages(){
        return pageRepository.findAllByOwnerNot(userService.currentUser.getUser()).stream().map(page -> {
            PageDto pageDto = new PageDto();
            pageDto.setName(page.getPageName());
            pageDto.setDescription(page.getDescription());
            pageDto.setWebsite(page.getWebsite());
            pageDto.setOwnerEmail(page.getOwner().getEmail());
            pageDto.setIsPagePublic(page.getIsPagePublic() ? "Public" : "Private");
            ResponseEntity<byte[]> b = getImage(page.getProfilePhoto().getFileName());
            pageDto.setProfilePhotoData(Base64.getEncoder().encodeToString(b.getBody()));
            pageDto.setLikeUsersEmails(page.getLikedPageUsers()
                    .stream()
                    .map(User::getEmail)
                    .collect(Collectors.toList()));
            return pageDto;
        })
                .collect(Collectors.toList());
    }
    public List<FriendDto> getCurrentUserFriends(){
        return currentUser.getUser().getFriendList().stream()
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

    public List<FriendDto> getCurrentUserNonFrinedsByCountry(){
        return userRepository.findNonFriendUsersByUserAndCountry(currentUser.getUser().getEmail()).stream()
                .map(user -> {
                    FriendDto friendDto = new FriendDto();
                    friendDto.setName(user.getName());
                    friendDto.setEmail(user.getEmail());
                    ResponseEntity<byte[]> b = getImage(user.getProfilePhoto().getFileName());
                    friendDto.setProfilePhotoData(Base64.getEncoder().encodeToString(b.getBody()));
                    return friendDto;
                })
                .limit(5)
                .collect(Collectors.toList());
    }

    public List<FriendDto> getAllUsers(){
        return userRepository.findAll().stream()
                .filter(user -> !user.getRole().getName().equals(RoleEnum.ROLE_ADMIN))
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
        List<FriendRequest> foundFriendRequest = friendRequestRepository.findAllByReceiverAndStatus(userService.currentUser.getUser(), FriendRequestStatusEnum.PENDING);
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
        userDto.setName(userService.currentUser.getUser().getName());
        userDto.setEmail(userService.currentUser.getUser().getEmail());
        userDto.setRole(userService.currentUser.getUser().getRole().getName().toString());
        userDto.setCountry(userService.currentUser.getUser().getCountry());
        userDto.setBirtdate(userService.currentUser.getUser().getBirtdate());
        userDto.setEducation(userService.currentUser.getUser().getEducation());
        ResponseEntity<byte[]> picture = getImage(userService.currentUser.getUser().getProfilePhoto().getFileName());
        userDto.setProfilePictureData(Base64.getEncoder().encodeToString(picture.getBody()));
        return userDto;
    }

    public PageDto getPageByName(String name){
        Page page = pageRepository.findAllByPageName(name);
        PageDto pageDto = new PageDto();
        pageDto.setName(page.getPageName());
        pageDto.setDescription(page.getDescription());
        pageDto.setWebsite(page.getWebsite());
        pageDto.setOwnerEmail(page.getOwner().getEmail());
        pageDto.setIsPagePublic(page.getIsPagePublic() ? "Public" : "Private");
        ResponseEntity<byte[]> b = getImage(page.getProfilePhoto().getFileName());
        pageDto.setProfilePhotoData(Base64.getEncoder().encodeToString(b.getBody()));
        pageDto.setLikeUsersEmails(page.getLikedPageUsers()
                .stream()
                .map(User::getEmail)
                .collect(Collectors.toList()));
        return pageDto;
    }

    public List<PageDto> getPagesYouMayLike(){
        return pageRepository.findAllByLikedPageUsersNotContainsAndIsPagePublic(userService.currentUser.getUser(), Boolean.TRUE).stream().map(page -> {
                    PageDto pageDto = new PageDto();
                    pageDto.setName(page.getPageName());
                    pageDto.setDescription(page.getDescription());
                    pageDto.setWebsite(page.getWebsite());
                    pageDto.setOwnerEmail(page.getOwner().getEmail());
                    pageDto.setIsPagePublic(page.getIsPagePublic() ? "Public" : "Private");
                    ResponseEntity<byte[]> b = getImage(page.getProfilePhoto().getFileName());
                    pageDto.setProfilePhotoData(Base64.getEncoder().encodeToString(b.getBody()));
                    pageDto.setLikeUsersEmails(page.getLikedPageUsers()
                            .stream()
                            .map(User::getEmail)
                            .collect(Collectors.toList()));
                    return pageDto;
                })
                .limit(5)
                .collect(Collectors.toList());
    }

    public List<PageDto> getCurrentUserPages(){
        return pageRepository.findAllByOwner(currentUser.getUser()).stream().map(page -> {
                    PageDto pageDto = new PageDto();
                    pageDto.setName(page.getPageName());
                    pageDto.setDescription(page.getDescription());
                    pageDto.setWebsite(page.getWebsite());
                    pageDto.setOwnerEmail(page.getOwner().getEmail());
                    pageDto.setIsPagePublic(page.getIsPagePublic() ? "Public" : "Private");
                    ResponseEntity<byte[]> b = getImage(page.getProfilePhoto().getFileName());
                    pageDto.setProfilePhotoData(Base64.getEncoder().encodeToString(b.getBody()));
                    pageDto.setLikeUsersEmails(page.getLikedPageUsers()
                            .stream()
                            .map(User::getEmail)
                            .collect(Collectors.toList()));
                    return pageDto;
                })
                .collect(Collectors.toList());
    }

    public UserDto getUserDtoByEmail(String email){
        User user = userRepository.findByEmail(email);
        UserDto userDto = new UserDto();
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        userDto.setRole(user.getRole().getName().toString());
        userDto.setCountry(user.getCountry());
        userDto.setBirtdate(user.getBirtdate());
        userDto.setEducation(user.getEducation());
        ResponseEntity<byte[]> picture = getImage(user.getProfilePhoto().getFileName());
        userDto.setProfilePictureData(Base64.getEncoder().encodeToString(picture.getBody()));
        return userDto;
    }

    public List<PhotoDto> getCurrentUserPhotos(){
        List<Photo> currentUserPhotos = photoRepository.findByUserOrderByDateUploadedDesc(userService.currentUser.getUser());
        return getPhotoDtos(currentUserPhotos, false);
    }

    public List<PhotoDto> getUserPhotosByEmail(String email){
        User user = userRepository.findByEmail(email);
        List<Photo> currentUserPhotos = photoRepository.findByUserOrderByDateUploadedDesc(user);
        return getPhotoDtos(currentUserPhotos, false);
    }

    public List<PhotoDto> getPagePhotosByPageName(String pageName){
        Page page = pageRepository.findAllByPageName(pageName);
        List<Photo> currentUserPhotos = photoRepository.findByPageOrderByDateUploadedDesc(page);
        return getPhotoDtos(currentUserPhotos, true);
    }

    private List<PhotoDto> getPhotoDtos(List<Photo> currentUserPhotos, Boolean isPage) {
        return currentUserPhotos.stream().map(photo -> {
            ResponseEntity<byte[]> picture = getImage(photo.getFileName());
            PhotoDto photoDto = new PhotoDto();
            photoDto.setData(Base64.getEncoder().encodeToString(picture.getBody()));
            ResponseEntity<byte[]> profilePicture;
            if (isPage) {
                photoDto.setUserName(photo.getPage().getPageName());
                profilePicture = getImage(photo.getPage().getProfilePhoto().getFileName());
            } else {
                photoDto.setUserName(photo.getUser().getName());
                profilePicture = getImage(photo.getUser().getProfilePhoto().getFileName());
            }
            photoDto.setFileName(photo.getFileName());
            List<String> likedPhotoUsersEmails = new ArrayList<>();
            photo.getLikedPhotoUsers().forEach(user -> likedPhotoUsersEmails.add(user.getEmail()));
            photoDto.setLikeUsersEmails(likedPhotoUsersEmails);
            photoDto.setNumberLikes(photo.getLikedPhotoUsers().size());
            photoDto.setDateUploaded(photo.getDateUploaded());
            photoDto.setPhotoComments(getPhotoCommentsDtos(photo));
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
        photoReport.setReporterUser(userService.currentUser.getUser());
        photoReport.setReportedPhoto(photoRepository.findByFileName(photoName));
        photoReport.setReportReason(ReportReasonEnum.valueOf(reason));
        photoReportRepository.save(photoReport);
    }

    public void uploadPhotoByPage(MultipartFile file,String status, String pageName) throws IOException {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        File tempFile = File.createTempFile("temp", fileName);
        file.transferTo(tempFile);

        uploadToS3(tempFile);
        Photo photo = new Photo();
        photo.setFileName(tempFile.getName());
        photo.setPage(pageRepository.findAllByPageName(pageName));
        photo.setStatus(status);
        photoRepository.save(photo);
    }

}
