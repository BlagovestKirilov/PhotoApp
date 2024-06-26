package com.example.photoapp.controller.user;

import com.example.photoapp.entity.User;
import com.example.photoapp.entity.dto.FriendDto;
import com.example.photoapp.entity.dto.ChangePasswordDto;
import com.example.photoapp.entity.dto.PageDto;
import com.example.photoapp.entity.dto.UserDto;
import com.example.photoapp.enums.ChangePasswordEnum;
import com.example.photoapp.service.impl.PhotoServiceImpl;
import com.example.photoapp.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
        model.addAttribute("notifications", userService.getCurrentUserNotification());
        List<FriendDto> users = photoServiceImpl.getFriendRequests();
        model.addAttribute("users", users);
        return "friendRequests";
    }

    @PostMapping("/add-friend")
    public String addFriendConfirm(@RequestParam String receiverEmail) {
        userService.addFriend(receiverEmail);
        return "redirect:/user/add-friend";
    }

    @GetMapping("/add-friend")
    public String addFriend(Model model) {
        model.addAttribute("currentUser", photoServiceImpl.getCurrentUserDto());
        model.addAttribute("currentUserChangePasswordEnum", ChangePasswordEnum.CHANGE_PASSWORD);
        model.addAttribute("notifications", userService.getCurrentUserNotification());
        List<FriendDto> friendDtos = photoServiceImpl.findNonFriendUsers();
        model.addAttribute("users", friendDtos);
        model.addAttribute("pages", photoServiceImpl.getCurrentUserNowOwnedPages());
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
        model.addAttribute("notifications", userService.getCurrentUserNotification());
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
        model.addAttribute("notifications", userService.getCurrentUserNotification());
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
        model.addAttribute("notifications", userService.getCurrentUserNotification());
        return "redirect:/user/friend-requests";
    }

    @GetMapping("/change-password")
    public String changePassword(Model model, @RequestParam String email, @RequestParam ChangePasswordEnum changePasswordEnum) {
        model.addAttribute("currentUser", photoServiceImpl.getCurrentUserDto());
        model.addAttribute("currentUserChangePasswordEnum", ChangePasswordEnum.CHANGE_PASSWORD);
        model.addAttribute("notifications", userService.getCurrentUserNotification());
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
    @GetMapping("/get-friends")
    public ResponseEntity<List<FriendDto>> getFriends( User currentUser) {
        List<FriendDto> friendDtos = photoServiceImpl.getCurrentUserFriends();
        return ResponseEntity.ok(friendDtos);
    }

    @PostMapping("/edit")
    public String editProfile(@ModelAttribute("user") UserDto user) {
        userService.editProfile(user);
        return "redirect:/profile?email=" + user.getEmail();
    }

    @PostMapping("/edit-page")
    public String editPage(@ModelAttribute("user") PageDto pageDto) {
        userService.editPage(pageDto);
        return "redirect:/page?name=" + pageDto.getNewPageName();
    }

    @PostMapping("/like-page")
    public String likePage(@RequestParam String pageName, @RequestParam String userEmail) {
        userService.likePage(pageName, userEmail);
        return "redirect:/page?name=" + pageName;
    }

    @PostMapping("/unlike-page")
    public String unlikePage(@RequestParam String pageName, @RequestParam String userEmail) {
        userService.unlikePage(pageName, userEmail);
        return "redirect:/page?name=" + pageName;
    }
}
