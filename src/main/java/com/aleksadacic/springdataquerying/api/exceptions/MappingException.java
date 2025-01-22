package com.aleksadacic.springdataquerying.api.exceptions;

public class MappingException extends RuntimeException {
    public MappingException(String message, Exception e) {
        super(message, e);
    }
}
