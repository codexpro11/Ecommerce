package com.yolo.productSite.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class homeController {
    @GetMapping("/api")

    public String greet() {
        return "welcome to homepage";
    }
}
