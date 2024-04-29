package com.example.photoapp.controller;

import com.example.photoapp.entities.dto.ChangePasswordDto;
import com.example.photoapp.entities.dto.ConfirmationDto;
import com.example.photoapp.entities.dto.LoginUserDto;
import com.example.photoapp.entities.dto.UserDto;
import com.example.photoapp.entities.User;
import com.example.photoapp.enums.ChangePasswordEnum;
import com.example.photoapp.enums.CodeConfirmationEnum;
import com.example.photoapp.enums.RegistrationStatusEnum;
import com.example.photoapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginForm(Model model) {
        // String ipAddress = request.getRemoteAddr();
        LoginUserDto user = new LoginUserDto();
        model.addAttribute("user", user);
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute("user") LoginUserDto userDto,
            BindingResult result,
            Model model) {
        User existingUser = userService.login(userDto);

        if (existingUser == null) {
            result.rejectValue("password", null,
                    "Wrong email or password!");
        }
        if (result.hasErrors()) {
            model.addAttribute("user", userDto);
            return "/login";
        }

        //userService.saveUser(userDto);
        return "redirect:/uploadForm";

    }

    @GetMapping("/logout")
    public String logout(Model model) {
        // String ipAddress = request.getRemoteAddr();
        userService.logout();
        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logout(
            @Valid @ModelAttribute("user") LoginUserDto userDto,
            BindingResult result,
            Model model) {
        userService.logout();

        return "redirect:/";
    }

    @GetMapping("/registration")
    public String registrationForm(Model model) {
        UserDto user = new UserDto();
        model.addAttribute("user", user);
        return "registration";
    }

    @PostMapping("/registration")
    public String registration(
            @Valid @ModelAttribute("user") UserDto userDto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        User existingUser = userService.findUserByEmail(userDto.getEmail());

        if (existingUser != null)
            result.rejectValue("email", null,
                    "User already registered !!!");

        if (result.hasErrors()) {
            model.addAttribute("user", userDto);
            return "/registration";
        }

        redirectAttributes.addAttribute("userEmail", userDto.getEmail());
        redirectAttributes.addAttribute("codeConfirmation", CodeConfirmationEnum.REGISTRATION);
        userService.saveUser(userDto);
        return "redirect:/confirm";
    }

    @GetMapping("/confirm")
    public String confirmLoginForm(@RequestParam("userEmail") String userEmail,@RequestParam("codeConfirmation") CodeConfirmationEnum codeConfirmation, Model model) {
        // String ipAddress = request.getRemoteAddr();
        ConfirmationDto confirmationDto = new ConfirmationDto();
        confirmationDto.setEmail(userEmail);
        confirmationDto.setCodeConfirmationEnum(codeConfirmation);
        model.addAttribute("confirmationDto", confirmationDto);
        return "codeConfirmation";
    }

    @PostMapping("/confirm")
    public String confirmLogin(
            @ModelAttribute("confirmationDto") ConfirmationDto confirmationDto,
            Model model,
            BindingResult result,
            RedirectAttributes redirectAttributes
    ) {
        Boolean isConfirmed = userService.confirmUser(confirmationDto);

        if (isConfirmed && confirmationDto.getCodeConfirmationEnum() == CodeConfirmationEnum.REGISTRATION) {
            userService.updateRegistrationStatus(confirmationDto.getEmail(), RegistrationStatusEnum.CONFIRMED);
        } else if (isConfirmed && confirmationDto.getCodeConfirmationEnum() == CodeConfirmationEnum.CHANGE_PASSWORD) {
            ChangePasswordDto changePasswordDto= new ChangePasswordDto();
            changePasswordDto.setEmail(confirmationDto.getEmail());
            changePasswordDto.setChangePasswordEnum(ChangePasswordEnum.FORGOT_PASSWORD);
            model.addAttribute("changePasswordDto", changePasswordDto);
            return "changePassword";
        } else {
            result.rejectValue("confirmationCode", null, "Wrong code!");
        }
        if (result.hasErrors()) {
            redirectAttributes.addAttribute("userEmail", confirmationDto.getEmail());
            model.addAttribute("confirmationDto", confirmationDto);
            return "codeConfirmation";
        }

            return "redirect:/";

    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgotPasswordEmail";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam("email") String userEmail, RedirectAttributes redirectAttributes) {
        userService.forgotPassword(userEmail);
        redirectAttributes.addAttribute("userEmail", userEmail);
        redirectAttributes.addAttribute("codeConfirmation", CodeConfirmationEnum.CHANGE_PASSWORD);
        return "redirect:/confirm";
    }
}
