package org.apereo.cas.permit;

import org.apereo.cas.services.RegisteredServiceAccessStrategyRequest;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PermitRegisteredServiceAccessStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("RegisteredService")
public class PermitRegisteredServiceAccessStrategyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "PermitRegisteredServiceAccessStrategy.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();
    
    @Test
    void verifySerializeToJson() throws IOException {
        val strategyWritten = new PermitRegisteredServiceAccessStrategy();
        strategyWritten.setAction(UUID.randomUUID().toString());
        strategyWritten.setApiKey(UUID.randomUUID().toString());
        strategyWritten.setResource(UUID.randomUUID().toString());
        strategyWritten.setTenant(UUID.randomUUID().toString());
        MAPPER.writeValue(JSON_FILE, strategyWritten);
        val read = MAPPER.readValue(JSON_FILE, PermitRegisteredServiceAccessStrategy.class);
        assertEquals(strategyWritten, read);
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "PERMIT_IO_API_KEY", matches = ".+")
    void verifyAccessOperation() throws Throwable {
        val strategy = new PermitRegisteredServiceAccessStrategy();
        strategy.setAction(UUID.randomUUID().toString());
        strategy.setApiKey(System.getenv("PERMIT_IO_API_KEY"));
        strategy.setResource(UUID.randomUUID().toString());
        strategy.setTenant(UUID.randomUUID().toString());

        val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        val attributes = new HashMap<String, List<Object>>();
        val username = UUID.randomUUID().toString();
        attributes.put("uid", List.of(username));
        attributes.put("firstname", List.of("bob"));
        attributes.put("lastname", List.of("king"));
        attributes.put("email", List.of("bob@example.org"));
        val accessRequest = RegisteredServiceAccessStrategyRequest.builder()
            .principalId(username)
            .attributes(attributes)
            .registeredService(service)
            .build();
        assertFalse(strategy.authorizeRequest(accessRequest));
    }
}
