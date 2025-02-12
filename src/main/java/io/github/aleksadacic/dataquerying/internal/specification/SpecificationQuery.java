package io.github.aleksadacic.dataquerying.internal.specification;

import io.github.aleksadacic.dataquerying.api.Query;
import io.github.aleksadacic.dataquerying.api.SearchOperator;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of the {@link Query} interface using a {@link Specification}-based approach.
 *
 * @param <T> the type of the entity being queried.
 */
public class SpecificationQuery<T> implements Query<T> {
    private Specification<T> specification;
    private boolean distinct = false;

    private SpecificationQuery() {
        this.specification = new SpecificationWrapper<>(null);
    }

    public static <T> Query<T> get() {
        return new SpecificationQuery<>();
    }

    public static <T> Query<T> get(Specification<T> specification) {
        SpecificationQuery<T> instance = new SpecificationQuery<>();
        instance.specification = Specification.where(specification);
        return instance;
    }

    public static <T> Query<T> get(Query<T> query) {
        SpecificationQuery<T> instance = new SpecificationQuery<>();
        instance.specification = Specification.where(query.buildSpecification());
        return instance;
    }

    public static <T> SpecificationQuery<T> where(String attribute, Object value) {
        return SpecificationQuery.where(attribute, SearchOperator.EQ, value);
    }

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
                if (predicate != null && SpecificationUtils.isNonTrivialPredicate(predicate, criteriaBuilder)) {
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
}
