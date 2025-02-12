package io.github.aleksadacic.dataquerying.internal.executor;

import io.github.aleksadacic.dataquerying.internal.specification.SpecificationEngine;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

class CriteriaQueryUtils {
    private CriteriaQueryUtils() {
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

    static <T> void applySorting(Pageable pageable, Root<T> root, CriteriaBuilder criteriaBuilder, CriteriaQuery<Tuple> criteriaQuery) {
        if (pageable == null) return;
        if (pageable.getSort().isSorted()) {
            List<Order> orders = pageable.getSort().stream()
                    .map(order -> buildOrder(root, criteriaBuilder, order))
                    .toList();

            criteriaQuery.orderBy(orders);
        }
    }

    private static <T> Order buildOrder(Root<T> root, CriteriaBuilder criteriaBuilder, Sort.Order order) {
        String property = order.getProperty();
        Path<?> path = property.contains(".")
                ? buildJoinedPath(root, property)
                : root.get(property);

        return order.isAscending()
                ? criteriaBuilder.asc(path)
                : criteriaBuilder.desc(path);
    }

    private static <T> Path<?> buildJoinedPath(Root<T> root, String propertyPath) {
        String[] attributes = propertyPath.split("\\.");
        From<?, ?> joinPath = root;

        for (int i = 0; i < attributes.length - 1; i++) {
            joinPath = joinPath.join(attributes[i], JoinType.LEFT);
        }

        return joinPath.get(attributes[attributes.length - 1]);
    }
}
