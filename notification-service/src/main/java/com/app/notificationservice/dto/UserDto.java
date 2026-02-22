package com.app.notificationservice.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private UUID id;

    private String email;

    private String role;

    private boolean active;
}