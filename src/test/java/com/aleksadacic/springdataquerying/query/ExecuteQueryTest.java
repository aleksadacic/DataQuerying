package com.aleksadacic.springdataquerying.query;

import com.aleksadacic.springdataquerying.api.Query;
import com.aleksadacic.springdataquerying.api.SearchOperator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class ExecuteQueryTest {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class Dto {
        String name;
        Integer age;
        String nickname;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class DtoMinimal {
        String name;
        Integer age;
    }

    @Test
    void testExecuteQuery() {
        // Arrange
        EntityManager entityManager = mock(EntityManager.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        CriteriaQuery<Object[]> criteriaQuery = mock(CriteriaQuery.class);
        Root<Dto> root = mock(Root.class);
        TypedQuery<Object[]> typedQuery = mock(TypedQuery.class);

        // Mock EntityManager behavior
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Object[].class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Dto.class)).thenReturn(root);

        // Mock TypedQuery behavior
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);

        // Mock result list from query
        Object[] dtoData1 = {"John", 30};
        Object[] dtoData2 = {"Jane", 25};
        when(typedQuery.getResultList()).thenReturn(List.of(dtoData1, dtoData2));

        // Create a Query instance and add filtering logic (optional)
        Query<Dto> query = Query.where("age", SearchOperator.GT, 20); // This is just an example filter

        // Act
        List<DtoMinimal> result = query.executeQuery(entityManager, Dto.class, DtoMinimal.class);

        // Assert
        assertNotNull(result, "The result list should not be null");
        assertEquals(2, result.size(), "The result list size should match the number of returned DTOs");

        // Verify individual results
        DtoMinimal dto1 = result.getFirst();
        assertEquals("John", dto1.getName());
        assertEquals(30, dto1.getAge());

        DtoMinimal dto2 = result.get(1);
        assertEquals("Jane", dto2.getName());
        assertEquals(25, dto2.getAge());

        // Verify interactions with mocks
        verify(entityManager).getCriteriaBuilder();
        verify(criteriaBuilder).createQuery(Object[].class);
        verify(criteriaQuery).from(Dto.class);
        verify(typedQuery).getResultList();
    }

}
