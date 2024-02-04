package com.example.photoapp.controller;

import com.example.photoapp.entities.dto.FriendRequestDto;
import com.example.photoapp.service.impl.CustomUserDetailsService;
import com.example.photoapp.service.impl.UserServiceImpl;
import com.example.photoapp.service.impl.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Controller
public class PhotoController {

    @Autowired PhotoService photoService;

    @Autowired CustomUserDetailsService customUserDetailsService;

    @Autowired UserServiceImpl userService;


    @GetMapping("/")
    public String showUploadForm(Model model) {
        model.addAttribute("photos", photoService.getFromS3());
        model.addAttribute("currentUser", customUserDetailsService.currentUser.getName());
        return "uploadForm";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        File tempFile = File.createTempFile("temp", fileName);
        file.transferTo(tempFile);

        photoService.uploadToS3(tempFile);
        photoService.save(tempFile);
        return "redirect:/";
    }

    @PostMapping("/remove-photo")
    public String removePhotoConfirm(@RequestParam String photoName) {
        photoService.deleteFromS3(photoName);
        return "redirect:/";
    }

    @GetMapping("/remove-photo")
    public String removePhoto() {
        return "removePhoto";
    }

}