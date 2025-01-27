package com.aleksadacic.springdataquerying.api;

import com.aleksadacic.springdataquerying.internal.specification.Filter;
import com.aleksadacic.springdataquerying.internal.specification.SpecificationEngine;
import com.aleksadacic.springdataquerying.internal.specification.SpecificationWrapper;
import com.aleksadacic.springdataquerying.internal.utils.QueryUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class Query<T> {
    private Specification<T> specification;
    private boolean distinct = false;

    private Query() {
        this.specification = new SpecificationWrapper<>(null);
    }

    public static <T> Query<T> get() {
        return new Query<>();
    }

    public static <T> Query<T> where(Query<T> query) {
        Query<T> instance = new Query<>();
        instance.specification = Specification.where(query.buildSpecification());
        return instance;
    }

    // Adds a simple where condition with default equality operator
    public static <T> Query<T> where(String attribute, Object value) {
        return Query.where(attribute, SearchOperator.EQ, value);
    }

    // Adds a condition with a specified operator
    public static <T> Query<T> where(String attribute, SearchOperator operator, Object value) {
        Query<T> instance = new Query<>();
        instance.specification = new SpecificationWrapper<>(new Filter(attribute, operator, value));
        return instance;
    }

    // Adds an AND condition
    public Query<T> and(String attribute, Object value) {
        return and(attribute, SearchOperator.EQ, value);
    }

    // Adds an AND condition with a specified operator
    public Query<T> and(String attribute, SearchOperator operator, Object value) {
        Specification<T> newSpec = new SpecificationWrapper<>(new Filter(attribute, operator, value));
        this.specification = this.specification.and(newSpec);
        return this;
    }

    // Adds an AND condition using another Specification
    public Query<T> and(Query<T> query) {
        this.specification = this.specification.and(query.buildSpecification());
        return this;
    }

    // Adds an OR condition
    public Query<T> or(String attribute, Object value) {
        return or(attribute, SearchOperator.EQ, value);
    }

    // Adds an OR condition with a specified operator
    public Query<T> or(String attribute, SearchOperator operator, Object value) {
        Specification<T> newSpec = new SpecificationWrapper<>(new Filter(attribute, operator, value));
        this.specification = this.specification.or(newSpec);
        return this;
    }

    // Adds an OR condition using another Specification
    public Query<T> or(Query<T> query) {
        this.specification = this.specification.or(query.buildSpecification());
        return this;
    }

    // Adds a join condition (handles both simple and nested joins)
    public Query<T> join(String joinAttribute, JoinType joinType) {
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

        this.specification = this.specification == null
                ? joinSpec
                : this.specification.and(joinSpec);

        return this;
    }

    public Query<T> distinct() {
        this.distinct = true;
        return this;
    }

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

    // Utility method to check if a predicate is trivial
    private boolean isNonTrivialPredicate(Predicate predicate, CriteriaBuilder criteriaBuilder) {
        // `criteriaBuilder.conjunction()` translates to 1=1
        return predicate == null || !predicate.equals(criteriaBuilder.conjunction());
    }

    // Method to execute the query with EntityManager
    public <R> List<R> executeQuery(EntityManager entityManager, Class<T> entityType, Class<R> dtoClass) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createTupleQuery();
        Root<T> root = criteriaQuery.from(entityType);

        if (this.distinct) {
            criteriaQuery.distinct(true);
        }

        // Apply the specification to build predicates and select fields
        if (specification != null) {
            Predicate predicate = specification.toPredicate(root, criteriaQuery, criteriaBuilder);
            if (predicate != null) {
                criteriaQuery.where(predicate);
            }
        }

        // Create a SpecificationWrapper with the selected fields and apply them
        SpecificationEngine.applySelection(root, criteriaQuery, criteriaBuilder, dtoClass);

        // Execute the query
        TypedQuery<Tuple> query = entityManager.createQuery(criteriaQuery);
        List<Tuple> results = query.getResultList();

        List<Map<String, Object>> mappedResults = QueryUtils.mapTuplesToFieldValues(results, dtoClass);

        // Map the results to DTOs using reflection
        ObjectMapper mapper = new ObjectMapper();
        return QueryUtils.convertToDtoList(dtoClass, mappedResults, mapper);
    }
}
