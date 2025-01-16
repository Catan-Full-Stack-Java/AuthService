package com.dzieger.dtos;

public class OutgoingPlayerDTO {

    private String firstName;
    private String username;
    private String role;

    public OutgoingPlayerDTO() {
    }

    public OutgoingPlayerDTO(String firstName, String username, String role) {
        this.firstName = firstName;
        this.username = username;
        this.role = role;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
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

    @Override
    public String toString() {
        return "OutgoingPlayerDTO{" +
                "firstName='" + firstName + '\'' +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
