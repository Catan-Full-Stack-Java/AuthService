package com.dzieger.exceptions.jwt;

public class JwtExpiredException extends JwtValidationException{

    public JwtExpiredException(String message) {
        super(message);
    }

    public JwtExpiredException(String message, Throwable cause) {
        super(message, cause);
    }

    public JwtExpiredException() {
        super("Jwt expired");
    }

}
