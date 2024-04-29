package com.example.photoapp.controller;

import com.example.photoapp.entities.dto.ChangePasswordDto;
import com.example.photoapp.enums.ChangePasswordEnum;
import com.example.photoapp.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserServiceImpl userService;

    @GetMapping("/")
    public String registrationForm() {
        return "redirect:/";
    }

    @GetMapping("/friend-requests")
    public String showFriendRequests(Model model) {
        model.addAttribute("requests", userService.getFriendRequests(userService.currentUser.getId()));
        return "friendRequests";
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
        return "removeFriend";
    }

    @GetMapping("/remove-friend")
    public String removeFriend() {
        return "removeFriend";
    }

    @PostMapping("/confirm-friend")
    public String confirmFriend(@RequestParam Long friendRequestId) {
        userService.confirmFriendRequest(friendRequestId);
        return "redirect:/user/friend-requests";
    }

    @GetMapping("/confirm-friend")
    public String confirmFriend() {
        return "redirect:/user/friend-requests";
    }

    @PostMapping("/reject-friend")
    public String rejectFriend(@RequestParam Long friendRequestId) {
        userService.rejectFriendRequest(friendRequestId);
        return "redirect:/user/friend-requests";
    }

    @GetMapping("/reject-friend")
    public String rejectFriend() {
        return "redirect:/user/friend-requests";
    }

    @GetMapping("/change-password")
    public String changePassword(Model model, @RequestParam String email, @RequestParam ChangePasswordEnum changePasswordEnum) {
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
