package org.apereo.cas.services;

import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OpenFGARegisteredServiceAccessStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("RegisteredService")
@ExtendWith(CasTestExtension.class)
class OpenFGARegisteredServiceAccessStrategyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifySerializeToJson() throws IOException {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val strategyWritten = new OpenFGARegisteredServiceAccessStrategy();
        strategyWritten.setApiUrl("https://localhost:8080");
        strategyWritten.setRelation("owner");
        strategyWritten.setObject("my-document");
        strategyWritten.setToken(UUID.randomUUID().toString());
        MAPPER.writeValue(jsonFile, strategyWritten);
        val read = MAPPER.readValue(jsonFile, OpenFGARegisteredServiceAccessStrategy.class);
        assertEquals(strategyWritten, read);
    }

    @Test
    void verifyOperation() throws Throwable {
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
        assertFalse(strategy.authorizeRequest(request));

        val data = mapper.writeValueAsString(CollectionUtils.wrap("allowed", true));
        try (val webServer = new MockWebServer(8755,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertTrue(strategy.authorizeRequest(request));
        }
    }

}
