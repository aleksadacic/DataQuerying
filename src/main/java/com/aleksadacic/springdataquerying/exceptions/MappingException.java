package com.aleksadacic.springdataquerying.exceptions;

public class MappingException extends RuntimeException {
    public MappingException(String message, Exception e) {
        super(message, e);
    }
}
