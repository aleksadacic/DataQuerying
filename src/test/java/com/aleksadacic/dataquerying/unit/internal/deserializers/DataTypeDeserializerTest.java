package com.aleksadacic.dataquerying.unit.internal.deserializers;

import com.aleksadacic.dataquerying.internal.deserializers.DataTypeDeserializer;
import com.aleksadacic.dataquerying.internal.enums.DataType;
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

class DataTypeDeserializerTest {
    private AutoCloseable closeable;

    private DataTypeDeserializer deserializer;

    @Mock
    private JsonParser jsonParser;

    @Mock
    private DeserializationContext context;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        deserializer = new DataTypeDeserializer();
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testDeserializeValidValue_STRING() throws IOException {
        // Suppose DataType.STRING has .value = "STRING"
        when(jsonParser.getText()).thenReturn("STRING");
        DataType result = deserializer.deserialize(jsonParser, context);
        assertEquals(DataType.STRING, result);
    }

    @Test
    void testDeserializeValidValue_numberCaseInsensitive() throws IOException {
        // Suppose DataType.NUMBER has .value = "NUMBER"
        when(jsonParser.getText()).thenReturn("nUmBeR");
        DataType result = deserializer.deserialize(jsonParser, context);
        assertEquals(DataType.NUMBER, result);
    }

    @Test
    void testDeserializeInvalidValue_throwsException() throws IOException {
        when(jsonParser.getText()).thenReturn("FOO");
        assertThrows(IllegalArgumentException.class,
                () -> deserializer.deserialize(jsonParser, context));
    }
}
