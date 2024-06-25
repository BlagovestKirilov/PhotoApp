package com.example.photoapp.controller.user;

import com.example.photoapp.entity.Page;
import com.example.photoapp.entity.dto.FriendDto;
import com.example.photoapp.entity.dto.UserDto;
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
import java.util.List;
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
        model.addAttribute("notifications", userService.getCurrentUserNotification());
        List<FriendDto> friendDtos = photoServiceImpl.getCurrentUserNonFrinedsByCountry();
        model.addAttribute("peopleYouMayKnow", friendDtos);
        model.addAttribute("pagesYouMayLike", photoServiceImpl.getPagesYouMayLike());
        return "uploadForm";
    }

    @GetMapping("/profile")
    public String showProfile(Model model, @RequestParam(name = "email") String email) {
        model.addAttribute("photos", photoServiceImpl.getUserPhotosByEmail(email));
        model.addAttribute("user", photoServiceImpl.getUserDtoByEmail(email));
        model.addAttribute("currentUser", photoServiceImpl.getCurrentUserDto());
        model.addAttribute("currentUserChangePasswordEnum", ChangePasswordEnum.CHANGE_PASSWORD);
        model.addAttribute("notifications", userService.getCurrentUserNotification());
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

    @PostMapping("/upload-photo-page")
    public String uploadPhotoPage(@RequestParam("file") MultipartFile file, @RequestParam("status") String status, @RequestParam("pageName") String pageName) throws IOException {
        photoServiceImpl.uploadPhotoByPage(file,status,pageName);
        return "redirect:/page?name=" + pageName;
    }
    @PostMapping("/change-profile-picture-page")
    public String changeProfilePicturePage(@RequestParam("filePage") MultipartFile file, @RequestParam("statusPage") String status, @RequestParam("pageNameProfilePicture") String pageName) throws IOException {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        File tempFile = File.createTempFile("temp", fileName);
        file.transferTo(tempFile);

        photoServiceImpl.uploadToS3(tempFile);
        photoServiceImpl.changeProfilePicturePage(tempFile, status, pageName);
        return "redirect:/page?name=" + pageName;
    }
    @PostMapping("/remove-photo")
    public String removePhotoConfirm(@RequestParam String photoFileName) {
        photoServiceImpl.deleteFromS3(photoFileName);
        return "redirect:/remove-photo";
    }

    @GetMapping("/remove-photo")
    public String removePhoto(Model model) {
        UserDto currentUserDto = photoServiceImpl.getCurrentUserDto();
        model.addAttribute("currentUser", currentUserDto);
        model.addAttribute("currentUserChangePasswordEnum", ChangePasswordEnum.CHANGE_PASSWORD);
        model.addAttribute("notifications", userService.getCurrentUserNotification());
        model.addAttribute("photos",photoServiceImpl.getCurrentUserPhotos());
        return "removePhoto";
    }

    @PostMapping("/like-photo")
    public String likePhoto(@RequestParam("photoFileName") String fileName) {
        photoServiceImpl.likePhoto(fileName);
        return "redirect:/uploadForm";
    }

    @PostMapping("/unlike-photo")
    public String unlikePhoto(@RequestParam("photoFileName") String fileName) {
        photoServiceImpl.unLikePhoto(fileName);
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
        model.addAttribute("notifications", userService.getCurrentUserNotification());
        model.addAttribute("isThereActiveBans", userService.isThereActiveBans());
        return "uploadPhoto";
    }

    @GetMapping("/change-profile-picture")
    public String changeProfilePicture(Model model) {
        model.addAttribute("currentUser", photoServiceImpl.getCurrentUserDto());
        model.addAttribute("currentUserChangePasswordEnum", ChangePasswordEnum.CHANGE_PASSWORD);
        model.addAttribute("notifications", userService.getCurrentUserNotification());
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

    @GetMapping("/show-pages")
    public String showPages(Model model) {
        model.addAttribute("currentUser", photoServiceImpl.getCurrentUserDto());
        model.addAttribute("currentUserChangePasswordEnum", ChangePasswordEnum.CHANGE_PASSWORD);
        model.addAttribute("notifications", userService.getCurrentUserNotification());
        model.addAttribute("pagesDtos", photoServiceImpl.getCurrentUserPages());
        return "showPages";
    }

    @PostMapping("/create-page")
    public String createPage(@RequestParam("pageName") String pageName, Model model) {
        userService.savePage(pageName);
        return "redirect:/show-pages"; // Assuming this is the page that shows the user's pages
    }
    @GetMapping("/page")
    public String showPage(Model model, @RequestParam(name = "name") String name) {
        model.addAttribute("photos", photoServiceImpl.getPagePhotosByPageName(name));
        model.addAttribute("page", photoServiceImpl.getPageByName(name));
        model.addAttribute("currentUser", photoServiceImpl.getCurrentUserDto());
        model.addAttribute("currentUserChangePasswordEnum", ChangePasswordEnum.CHANGE_PASSWORD);
        model.addAttribute("notifications", userService.getCurrentUserNotification());
        return "page";
    }
}