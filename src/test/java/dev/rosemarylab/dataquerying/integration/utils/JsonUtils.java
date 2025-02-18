package dev.rosemarylab.dataquerying.integration.utils;

import dev.rosemarylab.dataquerying.api.SearchRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;

public class JsonUtils {
    private JsonUtils() {
    }

    public static SearchRequest loadSearchRequestFromJson(String filename) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            File file = ResourceUtils.getFile("classpath:search-request-test-files/" + filename);
            return mapper.readValue(file, SearchRequest.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load JSON file: " + filename, e);
        }
    }
}
