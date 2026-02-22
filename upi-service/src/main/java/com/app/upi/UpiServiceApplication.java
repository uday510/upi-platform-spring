package com.app.upi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class UpiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UpiServiceApplication.class, args);
    }

}
