package com.example.photoapp.controller.user;

import com.example.photoapp.enums.ChangePasswordEnum;
import com.example.photoapp.service.UserService;
import com.example.photoapp.service.impl.PhotoServiceImpl;
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
public class UserPhotoController {

    @Autowired
    private PhotoServiceImpl photoServiceImpl;

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String firstPage() {
        return "redirect:/login";
    }
    @GetMapping("/uploadForm")
    public String showUploadForm(Model model) {
        model.addAttribute("photos", photoServiceImpl.getFromS3());
        model.addAttribute("currentUser", photoServiceImpl.getCurrentUserDto());
        model.addAttribute("currentUserChangePasswordEnum", ChangePasswordEnum.CHANGE_PASSWORD);
        return "uploadForm";
    }

    @GetMapping("/profile")
    public String showProfile(Model model) {
        model.addAttribute("photos", photoServiceImpl.getCurrentUserPhotos());
        model.addAttribute("currentUser", photoServiceImpl.getCurrentUserDto());
        model.addAttribute("currentUserChangePasswordEnum", ChangePasswordEnum.CHANGE_PASSWORD);
        return "profile";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam("status") String status) throws IOException {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        File tempFile = File.createTempFile("temp", fileName);
        file.transferTo(tempFile);

        photoServiceImpl.uploadToS3(tempFile);
        photoServiceImpl.save(tempFile, status);
        return "redirect:/uploadForm";
    }

    @PostMapping("/remove-photo")
    public String removePhotoConfirm(@RequestParam String photoFileName) {
        photoServiceImpl.deleteFromS3(photoFileName);
        return "redirect:/";
    }

    @GetMapping("/remove-photo")
    public String removePhoto(Model model) {
        model.addAttribute("currentUser", photoServiceImpl.getCurrentUserDto());
        model.addAttribute("currentUserChangePasswordEnum", ChangePasswordEnum.CHANGE_PASSWORD);
        model.addAttribute("photos",photoServiceImpl.getCurrentUserPhotos());
        return "removePhoto";
    }

    @PostMapping("/like-photo")
    public String likePhoto(@RequestParam("photoFileName") String fileName) {
        photoServiceImpl.likePhoto(fileName);
        return "redirect:/uploadForm";
    }

    @PostMapping("/add-comment")
    public String addComment(@RequestParam("photoFileName") String fileName, @RequestParam("comment") String comment) {
        photoServiceImpl.addComment(fileName, comment);
        return "redirect:/uploadForm";
    }

    @GetMapping("/upload-photo")
    public String uploadPhoto(Model model) {
        model.addAttribute("currentUser", photoServiceImpl.getCurrentUserDto());
        model.addAttribute("currentUserChangePasswordEnum", ChangePasswordEnum.CHANGE_PASSWORD);
        model.addAttribute("isThereActiveBans", userService.isThereActiveBans());
        return "uploadPhoto";
    }

    @GetMapping("/change-profile-picture")
    public String changeProfilePicture(Model model) {
        model.addAttribute("currentUser", photoServiceImpl.getCurrentUserDto());
        model.addAttribute("currentUserChangePasswordEnum", ChangePasswordEnum.CHANGE_PASSWORD);
        return "changeProfilePicture";
    }
    @PostMapping("/change-profile-picture")
    public String changeProfilePicture(@RequestParam("file") MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        File tempFile = File.createTempFile("temp", fileName);
        file.transferTo(tempFile);

        photoServiceImpl.uploadToS3(tempFile);
        photoServiceImpl.changeProfilePicture(tempFile);
        return "redirect:/uploadForm";
    }

    @PostMapping("/report-photo")
    public String reportPhoto(@RequestParam("photoFileName") String photoFileName, @RequestParam("reason") String reason){
        photoServiceImpl.reportPhoto(photoFileName, reason);
        return "redirect:/uploadForm";
    }
}