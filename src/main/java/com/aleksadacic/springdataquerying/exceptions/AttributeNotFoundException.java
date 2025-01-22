package com.aleksadacic.springdataquerying.exceptions;

public class AttributeNotFoundException extends SpecificationBuilderException {
    public AttributeNotFoundException(String attributeName) {
        super("Attribute '" + attributeName + "' not found.");
    }
}
