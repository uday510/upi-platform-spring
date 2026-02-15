package com.app.upigatewayservice.fallback;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/upi")
    public ResponseEntity<String> upiFallback() {

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("UPI Service is temporarily unavailable");
    }
}
