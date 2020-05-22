package com.stevenkolamkuzhiyil.Covid19.exception;

public class OutdatedDataException extends RuntimeException {

    public OutdatedDataException(String message) {
        super(message);
    }
}
