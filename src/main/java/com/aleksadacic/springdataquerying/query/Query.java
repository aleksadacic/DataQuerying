package com.aleksadacic.springdataquerying.query;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class Query<T> {
    //TODO distinct
    //TODO da se brisu 1=1 statementi da bi tool delovao ozbiljnije
    private Specification<T> specification;

    private Query() {
        this.specification = new SpecificationWrapper<>(null);
    }

    public static <T> Query<T> get() {
        return new Query<>();
    }

    public Query<T> where(Query<T> query) {
        this.specification = Specification.where(query.buildSpecification());
        return this;
    }

    // Adds a simple where condition with default equality operator
    public Query<T> where(String attribute, Object value) {
        return where(attribute, FilterOperator.EQ, value);
    }

    // Adds a condition with a specified operator
    public Query<T> where(String attribute, FilterOperator operator, Object value) {
        Specification<T> newSpec = new SpecificationWrapper<>(new Filter(attribute, operator, value));
        this.specification = this.specification == null ? newSpec : this.specification.and(newSpec);
        return this;
    }

    // Adds an AND condition
    public Query<T> and(String attribute, Object value) {
        return and(attribute, FilterOperator.EQ, value);
    }

    // Adds an AND condition with a specified operator
    public Query<T> and(String attribute, FilterOperator operator, Object value) {
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
        return or(attribute, FilterOperator.EQ, value);
    }

    // Adds an OR condition with a specified operator
    public Query<T> or(String attribute, FilterOperator operator, Object value) {
        Specification<T> newSpec = new SpecificationWrapper<>(new Filter(attribute, operator, value));
        this.specification = this.specification.or(newSpec);
        return this;
    }

    // Adds an OR condition using another Specification
    public Query<T> or(Query<T> query) {
        this.specification = this.specification.or(query.buildSpecification());
        return this;
    }

    // Adds a join condition
    public Query<T> join(String joinAttribute, JoinType joinType) {
        Specification<T> joinSpec = (root, query, criteriaBuilder) -> {
            root.join(joinAttribute, joinType);
            return criteriaBuilder.conjunction(); // Return an always-true predicate
        };
        this.specification = this.specification.and(joinSpec);
        return this;
    }

    // Method to perform a nested join
    public Query<T> nestedJoin(String path, JoinType joinType) {
        Specification<T> joinSpec = (root, query, criteriaBuilder) -> {
            String[] attributes = path.split("\\.");
            Join<?, ?> join = null;

            // Iterate through each attribute in the path
            for (int i = 0; i < attributes.length; i++) {
                if (i == 0) {
                    // The first attribute joins directly from the root
                    join = root.join(attributes[i], joinType);
                } else {
                    // Subsequent attributes join from the previous join
                    join = join.join(attributes[i], joinType);
                }
            }

            return criteriaBuilder.conjunction(); // Return an always-true predicate
        };
        this.specification = this.specification.and(joinSpec);
        return this;
    }

    public Specification<T> buildSpecification() {
        return specification;
    }

    // Builds and returns a Specification<T> that can be used with repository.findAll
    public Specification<T> buildSpecification(String[] selectedFields) {
        return (root, query, criteriaBuilder) -> {
            // Apply the existing specification if any conditions are defined
            if (specification != null) {
                Predicate predicate = specification.toPredicate(root, query, criteriaBuilder);
                query.where(predicate);
            }

            SpecificationEngine.applySelection(root, query, List.of(selectedFields));

            return query.getRestriction(); // Return the restriction (predicate) built so far
        };
    }

    // Method to execute the query with EntityManager
    public List<Object[]> executeQuery(EntityManager entityManager, Class<T> entityType, String[] selectedFields) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> criteriaQuery = criteriaBuilder.createQuery(Object[].class);
        Root<T> root = criteriaQuery.from(entityType);

        // Apply the specification to build predicates and select fields
        if (specification != null) {
            Predicate predicate = specification.toPredicate(root, criteriaQuery, criteriaBuilder);
            criteriaQuery.where(predicate);
        }

        // Create a SpecificationWrapper with the selected fields and apply them
        SpecificationEngine.applySelection(root, criteriaQuery, List.of(selectedFields));
        TypedQuery<Object[]> query = entityManager.createQuery(criteriaQuery);
        return query.getResultList();
    }
}
