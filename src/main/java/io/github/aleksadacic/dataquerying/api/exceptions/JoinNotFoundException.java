package io.github.aleksadacic.dataquerying.api.exceptions;

public class JoinNotFoundException extends SpecificationBuilderException {
    public JoinNotFoundException(String join) {
        super("Join '" + join + "' not found.");
    }
}
