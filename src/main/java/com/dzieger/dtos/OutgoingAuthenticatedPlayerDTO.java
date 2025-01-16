package com.dzieger.dtos;

public class OutgoingAuthenticatedPlayerDTO extends OutgoingPlayerDTO{

    private String token;

    public OutgoingAuthenticatedPlayerDTO() {
    }

    public OutgoingAuthenticatedPlayerDTO(String firstName, String username, String role, String token) {
        super(firstName, username, role);
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "OutgoingAuthenticatedPlayerDTO{" +
                "firstName='" + getFirstName() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", role='" + getRole() + '\'' +
                ", token='" + token + '\'' +
                '}';
    }

}
