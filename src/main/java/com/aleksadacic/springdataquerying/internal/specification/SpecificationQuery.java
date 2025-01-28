package com.aleksadacic.springdataquerying.internal.specification;

import com.aleksadacic.springdataquerying.api.Query;
import com.aleksadacic.springdataquerying.api.SearchOperator;
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

        this.specification = this.specification == null
                ? joinSpec
                : this.specification.and(joinSpec);

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
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createTupleQuery();
        Root<T> root = criteriaQuery.from(entityClass);

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
        SpecificationEngine.applySelection(root, criteriaQuery, criteriaBuilder, pojo);

        // Execute the query
        TypedQuery<Tuple> query = entityManager.createQuery(criteriaQuery);
        List<Tuple> results = query.getResultList();

        List<Map<String, Object>> mappedResults = QueryUtils.mapTuplesToFieldValues(results, pojo);

        // Map the results to DTOs using reflection
        ObjectMapper mapper = new ObjectMapper();
        return QueryUtils.convertToDtoList(pojo, mappedResults, mapper);
    }

    // Utility method to check if a predicate is trivial
    private boolean isNonTrivialPredicate(Predicate predicate, CriteriaBuilder criteriaBuilder) {
        // `criteriaBuilder.conjunction()` translates to 1=1
        return predicate == null || !predicate.equals(criteriaBuilder.conjunction());
    }
}
