package com.example.photoapp.controller.admin;

import com.example.photoapp.entity.dto.FriendDto;
import com.example.photoapp.enums.ChangePasswordEnum;
import com.example.photoapp.service.impl.PhotoServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminPhotoController {
    @Autowired
    PhotoServiceImpl photoServiceImpl;

    @GetMapping("/reported-photo")
    public String showReportedPhoto(Model model) {
        model.addAttribute("reportedPhotos", photoServiceImpl.getReportedPhotos());
        model.addAttribute("currentUser", photoServiceImpl.getCurrentUserDto());
//        model.addAttribute("currentUserChangePasswordEnum", ChangePasswordEnum.CHANGE_PASSWORD);
        return "reportedPhoto";
    }

    @GetMapping("/all-photo")
    public String showAllPhoto(Model model) {
        model.addAttribute("photos", photoServiceImpl.getAllPhoto());
        model.addAttribute("currentUser", photoServiceImpl.getCurrentUserDto());
        model.addAttribute("currentUserChangePasswordEnum", ChangePasswordEnum.CHANGE_PASSWORD);
        return "uploadForm";
    }

    @GetMapping("/all-user")
    public String getAllFriends(Model model) {
        model.addAttribute("currentUser", photoServiceImpl.getCurrentUserDto());
        model.addAttribute("currentUserChangePasswordEnum", ChangePasswordEnum.CHANGE_PASSWORD);
        List<FriendDto> friendDtos = photoServiceImpl.getAllUsers();
        model.addAttribute("users", friendDtos);
        return "showFriend";
    }
}
