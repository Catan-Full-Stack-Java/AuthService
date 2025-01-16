package com.dzieger.dtos;

import jakarta.validation.constraints.NotBlank;

public class LoginDTO {

    @NotBlank(message = "{username.NotBlank}")
    private String username;

    @NotBlank(message = "{password.NotBlank}")
    private String password;

    public LoginDTO() {
    }

    public LoginDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
