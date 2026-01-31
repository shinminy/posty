package com.posty.fileapi.error;

public class StoredFileNotFoundException extends RuntimeException {

    public StoredFileNotFoundException(String fileName) {
        super(fileName + "not found.");
    }
}
