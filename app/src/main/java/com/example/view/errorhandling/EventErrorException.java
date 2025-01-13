package com.example.view.errorhandling;

public class EventErrorException extends Exception {

    public EventErrorException(String errorString) {
        super(errorString);
    }
}
