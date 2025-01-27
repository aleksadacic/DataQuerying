package com.aleksadacic.springdataquerying.internal.specification;

import com.aleksadacic.springdataquerying.api.exceptions.AttributeNotFoundException;
import com.aleksadacic.springdataquerying.api.exceptions.JoinNotFoundException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

class SpecUtils {
    private SpecUtils() {
    }

    // Utility method to retrieve the correct Path based on attribute
    public static <T> Path<?> getPath(Root<T> root, String attribute) throws AttributeNotFoundException, JoinNotFoundException {
        if (attribute.contains(".")) {
            String[] parts = attribute.split("\\.");
            Join<?, ?> join = root.getJoins().stream()
                    .filter(e -> e.getAttribute().getName().equals(parts[0]))
                    .findFirst()
                    .orElse(null);
            if (join == null) {
                throw new JoinNotFoundException(parts[0]);
            }

            Path<?> path = join;
            for (int i = 1; i < parts.length; i++) {
                try {
                    path = path.get(parts[i]);
                } catch (IllegalArgumentException e) {
                    throw new AttributeNotFoundException(parts[i]);
                }
            }
            return path;
        } else {
            try {
                return root.get(attribute);
            } catch (Exception e) {
                throw new AttributeNotFoundException(attribute);
            }
        }
    }
}
