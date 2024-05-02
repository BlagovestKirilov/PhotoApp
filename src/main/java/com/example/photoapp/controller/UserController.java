package com.example.photoapp.controller;

import com.example.photoapp.entity.dto.FriendDto;
import com.example.photoapp.entity.dto.ChangePasswordDto;
import com.example.photoapp.enums.ChangePasswordEnum;
import com.example.photoapp.service.impl.PhotoServiceImpl;
import com.example.photoapp.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserServiceImpl userService;

    @Autowired
    PhotoServiceImpl photoServiceImpl;

    @GetMapping("/")
    public String registrationForm() {
        return "redirect:/";
    }

    @GetMapping("/friend-requests")
    public String showFriendRequests(Model model) {
        model.addAttribute("currentUser", photoServiceImpl.getCurrentUserDto());
        model.addAttribute("currentUserChangePasswordEnum", ChangePasswordEnum.CHANGE_PASSWORD);
        List<FriendDto> d = photoServiceImpl.getFriendRequests();
        model.addAttribute("users", d);
        return "friendRequests";
    }

    @PostMapping("/add-friend")
    public String addFriendConfirm(@RequestParam String receiverEmail) {
        userService.addFriend(receiverEmail);
        return "redirect:/";
    }

    @GetMapping("/add-friend")
    public String addFriend(Model model) {
        model.addAttribute("currentUser", photoServiceImpl.getCurrentUserDto());
        model.addAttribute("currentUserChangePasswordEnum", ChangePasswordEnum.CHANGE_PASSWORD);
        List<FriendDto> friendDtos = photoServiceImpl.findNonFriendUsers();
        model.addAttribute("users", friendDtos);
        return "addFriend";
    }

    @PostMapping("/remove-friend")
    public String removeFriendConfirm(@RequestParam String removeUserEmail) {
        userService.removeFriend(removeUserEmail);
        return "showFriend";
    }

    @GetMapping("/show-friend")
    public String removeFriend(Model model) {
        model.addAttribute("currentUser", photoServiceImpl.getCurrentUserDto());
        model.addAttribute("currentUserChangePasswordEnum", ChangePasswordEnum.CHANGE_PASSWORD);
        List<FriendDto> friendDtos = photoServiceImpl.getCurrentUserFriends();
        model.addAttribute("users", friendDtos);
        return "showFriend";
    }

    @PostMapping("/confirm-friend")
    public String confirmFriend(@RequestParam String senderEmail) {
        userService.confirmFriendRequest(senderEmail);
        return "redirect:/user/friend-requests";
    }

    @GetMapping("/confirm-friend")
    public String confirmFriend(Model model) {
        model.addAttribute("currentUser", photoServiceImpl.getCurrentUserDto());
        model.addAttribute("currentUserChangePasswordEnum", ChangePasswordEnum.CHANGE_PASSWORD);
        return "redirect:/user/friend-requests";
    }

    @PostMapping("/reject-friend")
    public String rejectFriend(@RequestParam String senderEmail) {
        userService.rejectFriendRequest(senderEmail);
        return "redirect:/user/friend-requests";
    }

    @GetMapping("/reject-friend")
    public String rejectFriend(Model model) {
        model.addAttribute("currentUser", photoServiceImpl.getCurrentUserDto());
        model.addAttribute("currentUserChangePasswordEnum", ChangePasswordEnum.CHANGE_PASSWORD);
        return "redirect:/user/friend-requests";
    }

    @GetMapping("/change-password")
    public String changePassword(Model model, @RequestParam String email, @RequestParam ChangePasswordEnum changePasswordEnum) {
        model.addAttribute("currentUser", photoServiceImpl.getCurrentUserDto());
        model.addAttribute("currentUserChangePasswordEnum", ChangePasswordEnum.CHANGE_PASSWORD);
        ChangePasswordDto changePasswordDto= new ChangePasswordDto();
        changePasswordDto.setEmail(email);
        changePasswordDto.setChangePasswordEnum(changePasswordEnum);
        model.addAttribute("changePasswordDto", changePasswordDto);
        return "changePassword";
    }

    @PostMapping("/change-password")
    public String changePassword(@ModelAttribute("changePasswordDto") ChangePasswordDto changePasswordDto,
                                 BindingResult bindingResult, Model model) {
        userService.changePassword(changePasswordDto);
        return "redirect:/user/password-changed";
    }
}
