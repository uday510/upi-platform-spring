package com.app.upi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notify")
public class TestController {

    @GetMapping("/ping")
    public String ping() {
        return "UPI Service OK";
    }

}
