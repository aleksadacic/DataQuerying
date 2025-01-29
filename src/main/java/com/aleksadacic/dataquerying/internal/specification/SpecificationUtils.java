package com.aleksadacic.dataquerying.internal.specification;

import com.aleksadacic.dataquerying.api.exceptions.AttributeNotFoundException;
import com.aleksadacic.dataquerying.api.exceptions.JoinNotFoundException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

class SpecificationUtils {
    private SpecificationUtils() {
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
                join = tryToCreateJoin(root, parts);
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

    private static <T> Join<?, ?> tryToCreateJoin(Root<T> root, String[] parts) throws JoinNotFoundException {
        Join<?, ?> join = null;

        // We exclude the last part of the attribute because that should be the attribute name of the last joined entity.
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];

            if (join == null) {
                // Start from the root
                join = root.join(part, JoinType.LEFT);
            } else {
                // Continue joining from the current join
                join = join.join(part, JoinType.LEFT);
            }
        }

        if (join == null) throw new JoinNotFoundException(String.join(".", parts));
        return join;
    }
}
