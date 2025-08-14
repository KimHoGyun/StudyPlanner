package com.studyapp.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "API is working! Server is running correctly.";
    }

    @GetMapping("/health")
    public String health() {
        return "Server is healthy!";
    }
}