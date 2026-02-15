package com.app.authservice.dto;

import lombok.Data;

@Data
public class RegisterRequestDTO {

    private String email;
    private String password;

}
