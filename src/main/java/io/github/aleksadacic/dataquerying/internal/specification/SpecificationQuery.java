package io.github.aleksadacic.dataquerying.internal.specification;

import io.github.aleksadacic.dataquerying.api.Query;
import io.github.aleksadacic.dataquerying.api.SearchOperator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class SpecificationQuery<T> implements Query<T> {
    private Specification<T> specification;
    private boolean distinct = false;

    private SpecificationQuery() {
        this.specification = new SpecificationWrapper<>(null);
    }

    public static <T> Query<T> get() {
        return new SpecificationQuery<>();
    }

    public static <T> Query<T> where(Query<T> query) {
        SpecificationQuery<T> instance = new SpecificationQuery<>();
        instance.specification = Specification.where(query.buildSpecification());
        return instance;
    }

    // Adds a simple where condition with default equality operator
    public static <T> SpecificationQuery<T> where(String attribute, Object value) {
        return SpecificationQuery.where(attribute, SearchOperator.EQ, value);
    }

    // Adds a condition with a specified operator
    public static <T> SpecificationQuery<T> where(String attribute, SearchOperator operator, Object value) {
        SpecificationQuery<T> instance = new SpecificationQuery<>();
        instance.specification = new SpecificationWrapper<>(new Filter(attribute, operator, value));
        return instance;
    }

    @Override
    public SpecificationQuery<T> and(String attribute, Object value) {
        return and(attribute, SearchOperator.EQ, value);
    }

    @Override
    public SpecificationQuery<T> and(String attribute, SearchOperator operator, Object value) {
        Specification<T> newSpec = new SpecificationWrapper<>(new Filter(attribute, operator, value));
        this.specification = this.specification.and(newSpec);
        return this;
    }

    @Override
    public SpecificationQuery<T> and(Query<T> query) {
        this.specification = this.specification.and(query.buildSpecification());
        return this;
    }

    @Override
    public SpecificationQuery<T> or(String attribute, Object value) {
        return or(attribute, SearchOperator.EQ, value);
    }

    @Override
    public SpecificationQuery<T> or(String attribute, SearchOperator operator, Object value) {
        Specification<T> newSpec = new SpecificationWrapper<>(new Filter(attribute, operator, value));
        this.specification = this.specification.or(newSpec);
        return this;
    }

    @Override
    public SpecificationQuery<T> or(Query<T> query) {
        this.specification = this.specification.or(query.buildSpecification());
        return this;
    }

    @Override
    public SpecificationQuery<T> join(String joinAttribute, JoinType joinType) {
        Specification<T> joinSpec = (root, query, criteriaBuilder) -> {
            String[] attributes = joinAttribute.split("\\.");
            Join<?, ?> join = null;

            // Perform the join(s)
            for (int i = 0; i < attributes.length; i++) {
                if (i == 0) {
                    // First part joins directly from the root
                    join = root.join(attributes[i], joinType);
                } else {
                    // Subsequent parts join from the previous join
                    join = join.join(attributes[i], joinType);
                }
            }

            // Return null to avoid adding unnecessary predicates
            return null;
        };

        this.specification = this.specification == null ? joinSpec : this.specification.and(joinSpec);

        return this;
    }

    @Override

    public SpecificationQuery<T> distinct() {
        this.distinct = true;
        return this;
    }

    @Override

    public Specification<T> buildSpecification() {
        return (root, query, criteriaBuilder) -> {
            if (query == null) return null;

            List<Predicate> predicates = new ArrayList<>();

            if (this.distinct) {
                query = query.distinct(true);
            }

            // Collect predicates from the specification
            if (specification != null) {
                Predicate predicate = specification.toPredicate(root, query, criteriaBuilder);
                if (predicate != null && isNonTrivialPredicate(predicate, criteriaBuilder)) {
                    predicates.add(predicate);
                }
            }
            // Combine predicates if they are not empty
            if (!predicates.isEmpty()) {
                query = query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
            }

            return query.getRestriction();
        };
    }

    @Override
    public <R> List<R> executeQuery(EntityManager entityManager, Class<T> entityClass, Class<R> pojo) {
        Map.Entry<CriteriaQuery<Tuple>, Root<T>> preparedQueryObjects = ExecuteQueryUtils.prepareCriteriaQuery(entityManager, entityClass, pojo, distinct, specification);
        CriteriaQuery<Tuple> criteriaQuery = preparedQueryObjects.getKey();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        Root<T> root = preparedQueryObjects.getValue();

        // Execute the query
        TypedQuery<Tuple> query = entityManager.createQuery(criteriaQuery);
        List<Tuple> results = query.getResultList();

        List<Map<String, Object>> mappedResults = QueryUtils.mapTuplesToFieldValues(results, pojo);

        // Map the results to DTOs using reflection
        ObjectMapper mapper = new ObjectMapper();
        return QueryUtils.convertToDtoList(pojo, mappedResults, mapper);
    }

    @Override
    public <R> Page<R> executeQuery(EntityManager entityManager, Class<T> entityClass, Class<R> pojo, PageRequest pageRequest) {
        Map.Entry<CriteriaQuery<Tuple>, Root<T>> preparedQueryObjects = ExecuteQueryUtils.prepareCriteriaQuery(entityManager, entityClass, pojo, distinct, specification);
        CriteriaQuery<Tuple> criteriaQuery = preparedQueryObjects.getKey();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        Root<T> root = preparedQueryObjects.getValue();

        // Apply sorting with support for joined paths
        ExecuteQueryUtils.applySorting(pageRequest, root, criteriaBuilder, criteriaQuery);

        // Execute the query with pagination
        TypedQuery<Tuple> query = entityManager.createQuery(criteriaQuery);
        query.setFirstResult((int) pageRequest.getOffset());
        query.setMaxResults(pageRequest.getPageSize());

        List<Tuple> results = query.getResultList();

        // Count total elements for pagination metadata
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<T> countRoot = countQuery.from(entityClass);
        if (specification != null) {
            Predicate predicate = specification.toPredicate(countRoot, countQuery, criteriaBuilder);
            if (predicate != null) {
                countQuery.where(predicate);
            }
        }
        countQuery.select(criteriaBuilder.count(countRoot));
        Long totalElements = entityManager.createQuery(countQuery).getSingleResult();

        // Map the tuples to a list of maps with field values
        List<Map<String, Object>> mappedResults = QueryUtils.mapTuplesToFieldValues(results, pojo);

        // Map the results to DTOs using reflection
        ObjectMapper mapper = new ObjectMapper();
        List<R> content = QueryUtils.convertToDtoList(pojo, mappedResults, mapper);

        // Return a Page containing the content and pagination metadata
        return new PageImpl<>(content, pageRequest, totalElements);
    }

    // Utility method to check if a predicate is trivial
    private boolean isNonTrivialPredicate(Predicate predicate, CriteriaBuilder criteriaBuilder) {
        // `criteriaBuilder.conjunction()` translates to 1=1
        return predicate == null || !predicate.equals(criteriaBuilder.conjunction());
    }
}
