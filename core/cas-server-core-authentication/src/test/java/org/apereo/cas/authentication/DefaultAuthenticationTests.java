package org.apereo.cas.authentication;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for JSON Serialization
 *
 * @author David Rodriguez
 * @since 5.0.0
 */
@Tag("Authentication")
public class DefaultAuthenticationTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "defaultAuthentication.json");

    private ObjectMapper mapper;

    @BeforeEach
    public void initialize() {
        mapper = Jackson2ObjectMapperBuilder.json()
            .featuresToDisable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
        mapper.findAndRegisterModules();
    }

    @Test
    @SneakyThrows
    public void verifySerializeADefaultAuthenticationToJson() {
        val serviceWritten = CoreAuthenticationTestUtils.getAuthentication();
        mapper.writeValue(JSON_FILE, serviceWritten);
        val serviceRead = mapper.readValue(JSON_FILE, Authentication.class);
        assertEquals(serviceWritten, serviceRead);
    }
}
