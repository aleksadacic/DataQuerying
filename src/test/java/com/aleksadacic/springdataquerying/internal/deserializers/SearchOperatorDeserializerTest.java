package com.aleksadacic.springdataquerying.internal.deserializers;

import com.aleksadacic.springdataquerying.api.SearchOperator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class SearchOperatorDeserializerTest {
    private AutoCloseable closeable;

    private SearchOperatorDeserializer deserializer;

    @Mock
    private JsonParser jsonParser;

    @Mock
    private DeserializationContext context;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        deserializer = new SearchOperatorDeserializer();
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testDeserializeValidValue_EQ() throws IOException {
        // Suppose SearchOperator.EQ has .operator = "EQ"
        when(jsonParser.getText()).thenReturn("EQ");
        SearchOperator result = deserializer.deserialize(jsonParser, context);
        assertEquals(SearchOperator.EQ, result);
    }

    @Test
    void testDeserializeValidValue_likECaseInsensitive() throws IOException {
        // Suppose SearchOperator.LIKE has .operator = "LIKE"
        when(jsonParser.getText()).thenReturn("liKe");
        SearchOperator result = deserializer.deserialize(jsonParser, context);
        assertEquals(SearchOperator.LIKE, result);
    }

    @Test
    void testDeserializeInvalidValue_throwsException() throws IOException {
        when(jsonParser.getText()).thenReturn("INVALID");
        assertThrows(IllegalArgumentException.class,
                () -> deserializer.deserialize(jsonParser, context));
    }
}
