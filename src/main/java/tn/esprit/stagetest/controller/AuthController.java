package tn.esprit.stagetest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import tn.esprit.stagetest.dto.RegistrationDto;
import tn.esprit.stagetest.service.UserService;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new RegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") RegistrationDto registrationDto,
                               BindingResult result,
                               Model model) {

        if (userService.usernameExists(registrationDto.getUsername())) {
            result.rejectValue("username", "error.user", "Username is already taken");
        }

        if (userService.emailExists(registrationDto.getEmail())) {
            result.rejectValue("email", "error.user", "Email is already registered");
        }

        if (result.hasErrors()) {
            return "register";
        }

        userService.registerUser(registrationDto);
        return "redirect:/login?registered";
    }

    @GetMapping("/home")
    public String homePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("username", userDetails.getUsername());
        return "home";
    }
}
