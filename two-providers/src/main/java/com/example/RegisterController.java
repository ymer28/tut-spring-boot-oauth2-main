package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegisterController {
    @Autowired
    private UserRepository repo;
    @Autowired
    private UserService userService;
    @GetMapping("/register")
    public String showSignUpForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/process_register")
    public String ProcessRegistration(User user) {
//        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//        String encodedPassword = encoder.encode(user.getPassword());
        repo.save(user);
        return "process_register";
    }

}
