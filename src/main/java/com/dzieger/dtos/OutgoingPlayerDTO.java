package com.dzieger.dtos;

public class OutgoingPlayerDTO {

    private String firstName;
    private String lastName;
    private String username;
    private String role;

    public OutgoingPlayerDTO() {
    }

    public OutgoingPlayerDTO(String firstName, String lastName, String username, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.role = role;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
