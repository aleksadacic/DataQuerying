package dev.rosemarylab.dataquerying.unit.internal.deserializers;

import dev.rosemarylab.dataquerying.internal.deserializers.ConditionalOperatorDeserializer;
import dev.rosemarylab.dataquerying.internal.enums.ConditionalOperator;
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

class ConditionalOperatorDeserializerTest {
    private AutoCloseable closeable;

    private ConditionalOperatorDeserializer deserializer;

    @Mock
    private JsonParser jsonParser;

    @Mock
    private DeserializationContext context;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        deserializer = new ConditionalOperatorDeserializer();
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testDeserializeValidValue_AND() throws IOException {
        // Suppose ConditionalOperator has an "AND" whose .operator = "AND"
        when(jsonParser.getText()).thenReturn("AND");
        ConditionalOperator result = deserializer.deserialize(jsonParser, context);
        assertEquals(ConditionalOperator.AND, result);
    }

    @Test
    void testDeserializeValidValue_orCaseInsensitive() throws IOException {
        // Suppose ConditionalOperator has an "OR" whose .operator = "OR"
        when(jsonParser.getText()).thenReturn("oR");
        ConditionalOperator result = deserializer.deserialize(jsonParser, context);
        assertEquals(ConditionalOperator.OR, result);
    }

    @Test
    void testDeserializeInvalidValue_throwsException() throws IOException {
        when(jsonParser.getText()).thenReturn("XYZ");
        assertThrows(IllegalArgumentException.class,
                () -> deserializer.deserialize(jsonParser, context));
    }
}
