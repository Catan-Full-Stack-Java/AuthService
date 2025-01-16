package com.dzieger.dtos;

import com.dzieger.annotations.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterDTO {

    @NotBlank(message = "{firstName.NotBlank}")
    private String firstName;

    @NotBlank(message = "{email.NotBlank}")
    @Email(message = "{email.Email}")
    private String email;

    @NotBlank(message = "{password.NotBlank}")
    @ValidPassword(message = "{password.ValidPassword}")
    private String password;

    @NotBlank(message = "{username.NotBlank}")
    @Size(min = 3, max = 20, message = "{username.Size}")
    private String username;

    public RegisterDTO() {
    }

    public RegisterDTO(String firstName, String email, String password, String username) {
        this.firstName = firstName;
        this.email = email;
        this.password = password;
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "RegisterDTO{" +
                "firstName='" + firstName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
