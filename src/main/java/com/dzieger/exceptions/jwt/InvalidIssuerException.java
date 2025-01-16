package com.dzieger.exceptions.jwt;

public class InvalidIssuerException extends JwtValidationException{

    public InvalidIssuerException(String message) {
        super(message);
    }

    public InvalidIssuerException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidIssuerException() {
        super("Invalid issuer");
    }

}
