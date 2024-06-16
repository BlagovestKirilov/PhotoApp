package com.example.photoapp.config;

import com.amazonaws.util.IOUtils;
import com.example.photoapp.entity.Photo;
import com.example.photoapp.entity.Role;
import com.example.photoapp.enums.RoleEnum;
import com.example.photoapp.repository.PhotoRepository;
import com.example.photoapp.repository.RoleRepository;
import com.example.photoapp.service.impl.PhotoServiceImpl;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

@Component
public class StartupDataInitializer {

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PhotoServiceImpl photoServiceImpl;

    @PostConstruct
    public void init() throws IOException {
        String DEFAULT_PROFILE_PICTURE = "default_profile_picture.jpg";
        Photo profilePicture = photoRepository.findByFileNameEndsWith(DEFAULT_PROFILE_PICTURE);
        if (Objects.isNull(profilePicture)) {
            ClassPathResource resource = new ClassPathResource(DEFAULT_PROFILE_PICTURE);
            InputStream inputStream = resource.getInputStream();
            byte[] bytes = IOUtils.toByteArray(inputStream);

            MultipartFile multipartFile = new ByteArrayMultipartFile(bytes, DEFAULT_PROFILE_PICTURE);
            File tempFile = File.createTempFile("init", DEFAULT_PROFILE_PICTURE);
            multipartFile.transferTo(tempFile);

            photoServiceImpl.uploadToS3(tempFile);
            Photo photo = new Photo();
            photo.setFileName(tempFile.getName());
            photoRepository.save(photo);
        }

        if(roleRepository.count() == 0){
            Role userRole = new Role(RoleEnum.ROLE_USER);
            Role adminRole = new Role(RoleEnum.ROLE_ADMIN);
            roleRepository.saveAll(List.of(userRole,adminRole));
        }
    }
}