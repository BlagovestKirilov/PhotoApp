package com.example.photoapp.controller;

import com.example.photoapp.security.service.CustomUserDetailsService;
import com.example.photoapp.security.service.UserServiceImpl;
import com.example.photoapp.service.PhotoService;
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
import java.util.Objects;

@Controller
public class PhotoController {

    @Autowired
    PhotoService photoService;
    @Autowired
    CustomUserDetailsService customUserDetailsService;
    @Autowired
    UserServiceImpl userService;


    @GetMapping("/")
    public String showUploadForm(Model model) {
        model.addAttribute("photos", photoService.getFromS3());
        model.addAttribute("currentUser",customUserDetailsService.currentUser.getName());
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

    @PostMapping("/add-friend")
    public String addFriendConfirm(@RequestParam String friendName) {
        userService.addFriend(friendName);
        return "redirect:/";
    }
    @GetMapping("/add-friend")
    public String addFriend() {
        return "addFriend";
    }
    @PostMapping("/remove-friend")
    public String removeFriendConfirm(@RequestParam String friendName) {
        userService.removeFriend(friendName);
        return "redirect:/";
    }
    @GetMapping("/remove-friend")
    public String removeFriend() {
        return "removeFriend";
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