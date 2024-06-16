package com.example.photoapp.controller.admin;

import com.example.photoapp.entity.Page;
import com.example.photoapp.entity.dto.FriendDto;
import com.example.photoapp.entity.dto.UserBanDto;
import com.example.photoapp.enums.ChangePasswordEnum;
import com.example.photoapp.service.UserService;
import com.example.photoapp.service.impl.PhotoServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private PhotoServiceImpl photoServiceImpl;

    @Autowired
    private UserService userService;

    @GetMapping("/reported-photo")
    public String showReportedPhoto(Model model) {
        model.addAttribute("reportedPhotos", photoServiceImpl.getReportedPhotos());
        model.addAttribute("currentUser", photoServiceImpl.getCurrentUserDto());
        model.addAttribute("notifications", userService.getCurrentUserNotification());
        return "reportedPhoto";
    }

    @GetMapping("/all-photo")
    public String showAllPhoto(Model model) {
        model.addAttribute("photos", photoServiceImpl.getAllPhoto());
        model.addAttribute("currentUser", photoServiceImpl.getCurrentUserDto());
        model.addAttribute("currentUserChangePasswordEnum", ChangePasswordEnum.CHANGE_PASSWORD);
        model.addAttribute("notifications", userService.getCurrentUserNotification());
        return "uploadForm";
    }

    @GetMapping("/all-user")
    public String getAllUsers(Model model) {
        model.addAttribute("currentUser", photoServiceImpl.getCurrentUserDto());
        model.addAttribute("currentUserChangePasswordEnum", ChangePasswordEnum.CHANGE_PASSWORD);
        List<FriendDto> friendDtos = photoServiceImpl.getAllUsers();
        model.addAttribute("notifications", userService.getCurrentUserNotification());
        model.addAttribute("users", friendDtos);
        return "showFriend";
    }

    @GetMapping("/all-pages")
    public String getAllPages(Model model) {
        model.addAttribute("currentUser", photoServiceImpl.getCurrentUserDto());
        model.addAttribute("currentUserChangePasswordEnum", ChangePasswordEnum.CHANGE_PASSWORD);
        List<Page> pages = userService.getAllPages();
        model.addAttribute("pages", pages);
        model.addAttribute("notifications", userService.getCurrentUserNotification());
        return "showPages";
    }

    @GetMapping("/ban-user")
    public String getBanUser(@RequestParam("photoFileName") String photoFileName, Model model) {
        model.addAttribute("currentUser", photoServiceImpl.getCurrentUserDto());
        model.addAttribute("photoFileName", photoFileName);
        UserBanDto userBanDto = new UserBanDto();
        userBanDto.setPhotoFileName(photoFileName);
        model.addAttribute("userBanDto", userBanDto);
        model.addAttribute("notifications", userService.getCurrentUserNotification());
        return "banUser";
    }

    @PostMapping("/ban-user")
    public String postBanUser(@ModelAttribute("userBanDto") UserBanDto userBanDto) {
        userService.banUser(userBanDto);
        return "redirect:/admin/reported-photo";
    }
}
