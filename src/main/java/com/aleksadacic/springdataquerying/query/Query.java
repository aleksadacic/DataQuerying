package com.aleksadacic.springdataquerying;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class Query<T> {
    private Specification<T> specification;

    private Query() {
        this.specification = Specification.where(null);
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
    //TODO null, not null
}
