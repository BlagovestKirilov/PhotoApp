package com.example.photoapp.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.example.photoapp.entities.Photo;
import com.example.photoapp.entities.dto.PhotoDto;
import com.example.photoapp.repositories.PhotoRepository;
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
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private AmazonS3 s3Client;

    public void uploadToS3(File file) {
        String s3Key = file.getName();

        PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, s3Key, file);
        s3Client.putObject(putObjectRequest);
    }

    public void deleteFromS3(String filename) {
        Photo photo = photoRepository.findByFileName(filename);
        if (Objects.nonNull(photo)) {
            if (Objects.equals(photo.getUser().getId(), customUserDetailsService.currentUser.getId())) {
                s3Client.deleteObject(BUCKET_NAME, filename);
                photoRepository.delete(photo);
            }
        }
    }

    public List<PhotoDto> getFromS3() {

        List<Photo> photosInDatabase = photoRepository.findAll().stream()
                .filter(photo -> {
                    long photoUploaderId = photo.getUser().getId();
                    return customUserDetailsService.currentUser.getFriends().stream()
                            .anyMatch(friend -> friend.getId() == photoUploaderId)
                            || customUserDetailsService.currentUser.getId() == photoUploaderId;
                })
                .toList();

        return photosInDatabase.stream().map(photo -> {
            ResponseEntity<byte[]> b = getImage(photo.getFileName());
            PhotoDto photoDto = new PhotoDto();
            photoDto.setData(Base64.getEncoder().encodeToString(b.getBody()));
            photoDto.setUserName(photo.getUser().getName());
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
        photo.setUser(customUserDetailsService.currentUser);
        photoRepository.save(photo);
    }
}
