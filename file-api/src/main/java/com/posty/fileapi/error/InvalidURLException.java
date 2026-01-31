package com.posty.fileapi.error;

public class InvalidURLException extends RuntimeException {

    public InvalidURLException() {
        super("Invalid URL!");
    }
}
