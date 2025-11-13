package com.spring.Springweb.Controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        // Lấy thông tin user đang đăng nhập
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // username/email đã login

        model.addAttribute("message", "Hello " + email);

        return "home"; // render templates/home.html
    }
}
