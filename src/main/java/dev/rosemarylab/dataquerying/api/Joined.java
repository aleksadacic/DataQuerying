package dev.rosemarylab.dataquerying.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Joined {
    /**
     * E.g. location.city.municipality
     */
    String joinPath();
}
