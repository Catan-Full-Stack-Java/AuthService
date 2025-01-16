package com.dzieger.exceptions.jwt;

public class MalformedJwtException extends JwtValidationException{

    public MalformedJwtException(String message) {
        super(message);
    }

    public MalformedJwtException(String message, Throwable cause) {
        super(message, cause);
    }

    public MalformedJwtException() {
        super("Malformed JWT");
    }

}
