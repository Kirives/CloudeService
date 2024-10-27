package com.example.demo.controllers;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError() {
        // Логика перенаправления на главную страницу
        return "redirect:/";
    }

    // Если требуется, можно переопределить метод для возврата пути ошибки
    public String getErrorPath() {
        return "/error";
    }
}
