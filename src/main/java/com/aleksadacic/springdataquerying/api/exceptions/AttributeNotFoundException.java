package com.aleksadacic.springdataquerying.api.exceptions;

public class AttributeNotFoundException extends SpecificationBuilderException {
    public AttributeNotFoundException(String attributeName) {
        super("Attribute '" + attributeName + "' not found.");
    }
}
