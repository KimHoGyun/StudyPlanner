package com.studyapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class WelcomeController {

    @GetMapping("/")
    public Map<String, Object> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Study Management API Server");
        response.put("status", "running");
        response.put("version", "1.0.0");
        response.put("endpoints", new String[]{
                "/api/auth/test",
                "/api/auth/login",
                "/api/auth/signup",
                "/api/study-groups",
                "/api/attendance",
                "/h2-console"
        });
        return response;
    }
}