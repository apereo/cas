package org.apereo.cas.services;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OpenFGARegisteredServiceAccessStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("RegisteredService")
public class OpenFGARegisteredServiceAccessStrategyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "OpenFGARegisteredServiceAccessStrategy.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    public void verifySerializeToJson() throws IOException {
        val strategyWritten = new OpenFGARegisteredServiceAccessStrategy();
        strategyWritten.setApiUrl("https://localhost:8080");
        strategyWritten.setRelation("owner");
        strategyWritten.setObject("my-document");
        strategyWritten.setToken(UUID.randomUUID().toString());
        MAPPER.writeValue(JSON_FILE, strategyWritten);
        val read = MAPPER.readValue(JSON_FILE, OpenFGARegisteredServiceAccessStrategy.class);
        assertEquals(strategyWritten, read);
    }

    @Test
    public void verifyOperation() throws Exception {
        val mapper = JacksonObjectMapperFactory.builder().defaultTypingEnabled(false).build().toObjectMapper();
        val strategy = new OpenFGARegisteredServiceAccessStrategy();
        strategy.setApiUrl("http://localhost:8755");
        strategy.setRelation("reader");
        strategy.setStoreId("01GFTZWEZZMAM0NHQQZWE6AN3H");
        strategy.setToken(UUID.randomUUID().toString());
        strategy.setObject("document:Z");
        val request = RegisteredServiceAccessStrategyRequest.builder()
            .service(RegisteredServiceTestUtils.getService())
            .principalId("casuser")
            .build();
        assertFalse(strategy.doPrincipalAttributesAllowServiceAccess(request));

        val data = mapper.writeValueAsString(CollectionUtils.wrap("allowed", true));
        try (val webServer = new MockWebServer(8755,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertTrue(strategy.doPrincipalAttributesAllowServiceAccess(request));
        }
    }
    
}
