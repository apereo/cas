package org.apereo.cas.services;

import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is test cases for
 * {@link GroovyRegisteredServiceAccessStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Groovy")
public class GroovyRegisteredServiceAccessStrategyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "GroovyRegisteredServiceAccessStrategyTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();
    
    @Test
    public void checkDefaultAuthzStrategyConfig() {
        val authz = new GroovyRegisteredServiceAccessStrategy();
        authz.setGroovyScript("classpath:accessstrategy.groovy");

        authz.setServiceAccessAllowed(true);
        assertTrue(authz.isServiceAccessAllowed());
        assertTrue(authz.isServiceAccessAllowedForSso());
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess("test", new HashMap<>()));
        assertNull(authz.getUnauthorizedRedirectUrl());
        assertNotNull(authz.getDelegatedAuthenticationPolicy());
        assertNotNull(authz.getRequiredAttributes());
    }

    @Test
    public void verifySerializationToJson() throws IOException {
        val authz = new GroovyRegisteredServiceAccessStrategy();
        authz.setGroovyScript("classpath:accessstrategy.groovy");
        MAPPER.writeValue(JSON_FILE, authz);

        val strategyRead = MAPPER.readValue(JSON_FILE, GroovyRegisteredServiceAccessStrategy.class);
        assertEquals(authz, strategyRead);
    }
}
