package dev.rosemarylab.dataquerying.internal.specification;

import dev.rosemarylab.dataquerying.api.Joined;
import dev.rosemarylab.dataquerying.api.exceptions.SpecificationBuilderException;
import dev.rosemarylab.dataquerying.internal.utils.ReflectionUtils;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SpecificationEngine {

    private SpecificationEngine() {
    }

    public static Predicate eq(Filter filter, CriteriaBuilder criteriaBuilder, Path<?> fieldPath) {
        if (filter.getValue() == null) {
            return criteriaBuilder.isNull(fieldPath);
        }
        return criteriaBuilder.equal(fieldPath, filter.getValue());
    }

    public static Predicate notEq(Filter filter, CriteriaBuilder criteriaBuilder, Path<?> fieldPath) {
        if (filter.getValue() == null) {
            return criteriaBuilder.isNotNull(fieldPath);
        }
        return criteriaBuilder.notEqual(fieldPath, filter.getValue());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Predicate gte(Filter filter, CriteriaBuilder criteriaBuilder, Expression<? extends Comparable> fieldPath) {
        return criteriaBuilder.greaterThanOrEqualTo(
                fieldPath,
                (Comparable) filter.getValue()
        );
    }

    public static CriteriaBuilder.In<Object> in(Filter filter, CriteriaBuilder criteriaBuilder, Path<?> fieldPath) {
        if (!(filter.getValue() instanceof Collection<?> values)) {
            throw new SpecificationBuilderException("IN operator requires a collection of values");
        }
        CriteriaBuilder.In<Object> inClause = criteriaBuilder.in(fieldPath);
        for (Object value : values) {
            inClause.value(value);
        }
        return inClause;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Predicate between(Filter filter, CriteriaBuilder criteriaBuilder, Expression<? extends Comparable> fieldPath) {
        if (!(filter.getValue() instanceof List<?> valueList) || valueList.size() != 2) {
            throw new SpecificationBuilderException("BETWEEN operator requires a list of two comparable values");
        }
        Comparable lowerBound = (Comparable) valueList.get(0);
        Comparable upperBound = (Comparable) valueList.get(1);
        return criteriaBuilder.between(
                fieldPath,
                lowerBound,
                upperBound
        );
    }

    public static Predicate notLike(Filter filter, CriteriaBuilder criteriaBuilder, Path<?> fieldPath) {
        if (!(filter.getValue() instanceof String value)) {
            throw new SpecificationBuilderException("NOT_LIKE operator requires a String value");
        }
        return criteriaBuilder.notLike(fieldPath.as(String.class), "%" + value + "%");
    }

    public static Predicate like(Filter filter, CriteriaBuilder criteriaBuilder, Path<?> fieldPath) {
        if (!(filter.getValue() instanceof String value)) {
            throw new SpecificationBuilderException("LIKE operator requires a String value");
        }
        return criteriaBuilder.like(fieldPath.as(String.class), "%" + value + "%");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Predicate lt(Filter filter, CriteriaBuilder criteriaBuilder, Expression<? extends Comparable> fieldPath) {
        return criteriaBuilder.lessThan(
                fieldPath,
                (Comparable) filter.getValue()
        );
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Predicate gt(Filter filter, CriteriaBuilder criteriaBuilder, Expression<? extends Comparable> fieldPath) {
        return criteriaBuilder.greaterThan(
                fieldPath,
                (Comparable) filter.getValue()
        );
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Predicate lte(Filter filter, CriteriaBuilder criteriaBuilder, Expression<? extends Comparable> fieldPath) {
        return criteriaBuilder.lessThanOrEqualTo(
                fieldPath,
                (Comparable) filter.getValue()
        );
    }

    // Utility method to apply the selected fields to the CriteriaQuery
    public static <T, R> void applySelection(Root<T> root, CriteriaQuery<Tuple> query, CriteriaBuilder criteriaBuilder, Class<R> dtoClass) {
        Map<String, Joined> fields = ReflectionUtils.getAttributeNamesWithJoinedProperties(dtoClass);

        // Create selections for the selected fields from the root entity
        List<? extends Selection<?>> selections = fields.entrySet().stream()
                .map(entry -> getSelectionField(root, entry.getKey(), entry.getValue()))
                .toList();

        // Create a compound selection based on the ordered selections
        CompoundSelection<Tuple> compoundSelection = criteriaBuilder.tuple(selections.toArray(new Selection[0]));
        query.select(compoundSelection);
    }

//    TODO distinct on
    //select distinct on (l1_0.id) l1_0.first_name, l1_0.last_name, li2_0.url, po1_0.description, l1_0.id
    //from listing l1_0
    //         join user_listing_interaction uli1_0 on l1_0.id = uli1_0.listing_id
    //         join listing_image li2_0 on l1_0.id = li2_0.listing_id
    //         join listing_pick_one lpo2_0 on l1_0.id = lpo2_0.listing_id
    //         join pick_one po1_0 on po1_0.id = lpo2_0.pick_one_id
    //where uli1_0.user_id=2
    //  and uli1_0.interaction=1
    //  and l1_0.status=0
    //offset ? rows fetch first ? rows only

    private static <T> Selection<?> getSelectionField(Root<T> root, String field, Joined joined) {
        if (joined != null) {
            From<?, ?> joinPath = root;
            String[] joinPathParts = joined.joinPath().split("\\.");
            for (int i = 0; i < joinPathParts.length - 1; i++) {
                joinPath = joinPath.join(joinPathParts[i], JoinType.INNER);
            }
            Selection<?> selection = joinPath.get(joinPathParts[joinPathParts.length - 1]);
            return selection.alias(field);
        }
        Selection<?> selection = root.get(field);
        selection.alias(field);
        return selection;
    }
}
