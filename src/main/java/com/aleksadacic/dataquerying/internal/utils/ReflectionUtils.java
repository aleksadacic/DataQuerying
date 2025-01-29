package com.aleksadacic.dataquerying.internal.utils;

import java.util.Arrays;
import java.util.List;

public class ReflectionUtils {
    private ReflectionUtils() {
    }

    public static <T> List<String> getAttributeNamesFromGetters(Class<T> targetType) {
        return Arrays.stream(targetType.getDeclaredMethods())
                .filter(method -> method.getName().startsWith("get") && method.getParameterCount() == 0)
                .map(method -> {
                    // Convert "getXyz" to "xyz" (field name convention)
                    String name = method.getName().substring(3); // Remove "get"
                    return Character.toLowerCase(name.charAt(0)) + name.substring(1); // Lowercase first letter
                })
                .toList();
    }
}
