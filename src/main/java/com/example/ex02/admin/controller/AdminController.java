package com.example.ex02.admin.controller;

import com.example.ex02.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    public String adminHome(Model model) {
        model.addAttribute("stats", adminService.getDashboard());
        return "admin/dashboard";
    }
}
