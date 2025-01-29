package com.aleksadacic.dataquerying.internal.specification;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

class ExecuteQueryUtils {
    private ExecuteQueryUtils() {
    }

    static <T, R> Map.Entry<CriteriaQuery<Tuple>, Root<T>> prepareCriteriaQuery(EntityManager entityManager, Class<T> entityClass, Class<R> pojo, boolean distinct, Specification<T> specification) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createTupleQuery();
        Root<T> root = criteriaQuery.from(entityClass);

        if (distinct) {
            criteriaQuery.distinct(true);
        }

        // Apply the specification to build predicates
        if (specification != null) {
            Predicate predicate = specification.toPredicate(root, criteriaQuery, criteriaBuilder);
            if (predicate != null) {
                criteriaQuery.where(predicate);
            }
        }

        // Apply the selection fields
        SpecificationEngine.applySelection(root, criteriaQuery, criteriaBuilder, pojo);

        return Map.entry(criteriaQuery, root);
    }

    static <T> void applySorting(PageRequest pageRequest, Root<T> root, CriteriaBuilder criteriaBuilder, CriteriaQuery<Tuple> criteriaQuery) {
        if (pageRequest == null) return;
        if (pageRequest.getSort().isSorted()) {
            List<Order> orders = pageRequest.getSort().stream().map(order -> {
                String property = order.getProperty();
                Path<?> path;

                if (property.contains(".")) {
                    // Handle joined paths (e.g., "user.role.roleName")
                    String[] attributes = property.split("\\.");
                    From<?, ?> joinPath = root;

                    for (int i = 0; i < attributes.length - 1; i++) {
                        joinPath = joinPath.join(attributes[i], JoinType.LEFT); // Perform left join for each part of the path
                    }

                    path = joinPath.get(attributes[attributes.length - 1]); // Get the final attribute for sorting
                } else {
                    // Simple attribute, no join required
                    path = root.get(property);
                }

                return order.isAscending() ? criteriaBuilder.asc(path) : criteriaBuilder.desc(path);
            }).toList();

            criteriaQuery.orderBy(orders);
        }
    }
}
