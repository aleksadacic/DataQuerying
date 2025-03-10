package dev.rosemarylab.dataquerying.api.exceptions;

public class JoinNotFoundException extends SpecificationBuilderException {
    public JoinNotFoundException(String join) {
        super("Join '" + join + "' not found.");
    }
}
