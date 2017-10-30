package org.apereo.cas.authentication;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Test for JSON Serialization
 * @author David Rodriguez
 * @since 5.0.0
 */
public class DefaultAuthenticationTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "defaultAuthentication.json");

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = Jackson2ObjectMapperBuilder.json()
                .featuresToDisable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        mapper.findAndRegisterModules();
    }
    
    @Test
    public void verifySerializeADefaultAuthenticationToJson() throws IOException {
        final Authentication serviceWritten = CoreAuthenticationTestUtils.getAuthentication();
        mapper.writeValue(JSON_FILE, serviceWritten);
        final Authentication serviceRead = mapper.readValue(JSON_FILE, Authentication.class);
        assertEquals(serviceWritten, serviceRead);
    }
}
