package com.example.demo.controllers;

import com.example.demo.models.User;
import com.example.demo.services.RegistrationService;
import com.example.demo.services.UserService;
import io.minio.errors.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Controller
@RequestMapping
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private RegistrationService registrationService;

    @GetMapping("/login")
    public String login(Model model, @RequestParam(value = "error",required = false) String error) {
        if(error != null) {
            model.addAttribute("error","Такой комбинации логина и пароля не существует");
        }
        model.addAttribute("loginUser", new User());
        return "login";
    }

    @PostMapping("/login")
    public String loginPost(@Valid @ModelAttribute("loginUser") User user, BindingResult result, Model model) {
        if(result.hasErrors()) {
            model.addAttribute("loginUser", user);
            return "login";
        }
        return "forward:/perform_login";
    }

    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("newUser", new User());
        return "registration";
    }

    @PostMapping("/registration")
    public String registrationPost(@Valid @ModelAttribute("newUser") User user, BindingResult bindingResult, Model model, HttpServletRequest request) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        if (bindingResult.hasErrors()) {
            model.addAttribute("newUser", user);
            return "registration";
        }
        String rawPassword = user.getPassword();
        try {
            userService.addUser(user);
        }catch (Exception e) {
            model.addAttribute("error",e.getMessage());
            model.addAttribute("newUser", user);
            return "registration";
        }
        registrationService.authenticate(user.getUsername(),rawPassword,request);
        registrationService.createFolder(String.valueOf(user.getId()));
        return "redirect:/";
    }

}
