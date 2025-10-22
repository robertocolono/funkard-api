package com.funkard.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String email;
    private String username;
    private String name;
    private String avatarUrl;
    private String role;
    private String preferredCurrency; // 👈 nuovo campo
}