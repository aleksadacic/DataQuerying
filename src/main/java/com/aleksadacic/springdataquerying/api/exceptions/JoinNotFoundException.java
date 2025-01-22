package com.aleksadacic.springdataquerying.api.exceptions;

public class JoinNotFoundException extends SpecificationBuilderException {
    public JoinNotFoundException(String join) {
        super("Join '" + join + "' not found.");
    }
}
