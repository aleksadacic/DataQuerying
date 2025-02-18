package dev.rosemarylab.dataquerying.unit.internal.deserializers;

import dev.rosemarylab.dataquerying.internal.deserializers.SortOrderDeserializer;
import dev.rosemarylab.dataquerying.internal.enums.SortOrder;
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

class SortOrderDeserializerTest {
    private AutoCloseable closeable;

    private SortOrderDeserializer deserializer;

    @Mock
    private JsonParser jsonParser;

    @Mock
    private DeserializationContext context;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        deserializer = new SortOrderDeserializer();
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testDeserializeValidValue_ASC() throws IOException {
        // Suppose SortOrder.ASC has .value = "ASC"
        when(jsonParser.getText()).thenReturn("ASC");
        SortOrder result = deserializer.deserialize(jsonParser, context);
        assertEquals(SortOrder.ASC, result);
    }

    @Test
    void testDeserializeValidValue_descCaseInsensitive() throws IOException {
        // Suppose SortOrder.DESC has .value = "DESC"
        when(jsonParser.getText()).thenReturn("dEsC");
        SortOrder result = deserializer.deserialize(jsonParser, context);
        assertEquals(SortOrder.DESC, result);
    }

    @Test
    void testDeserializeInvalidValue_throwsException() throws IOException {
        when(jsonParser.getText()).thenReturn("XYZ");
        assertThrows(IllegalArgumentException.class,
                () -> deserializer.deserialize(jsonParser, context));
    }
}
