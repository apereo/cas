package org.apereo.cas.permit;

import module java.base;
import org.apereo.cas.services.RegisteredServiceAccessStrategyRequest;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PermitRegisteredServiceAccessStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("RegisteredService")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
class PermitRegisteredServiceAccessStrategyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifySerializeToJson() throws IOException {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val strategyWritten = new PermitRegisteredServiceAccessStrategy();
        strategyWritten.setAction(UUID.randomUUID().toString());
        strategyWritten.setApiKey(UUID.randomUUID().toString());
        strategyWritten.setResource(UUID.randomUUID().toString());
        strategyWritten.setTenant(UUID.randomUUID().toString());
        MAPPER.writeValue(jsonFile, strategyWritten);
        val read = MAPPER.readValue(jsonFile, PermitRegisteredServiceAccessStrategy.class);
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
            .applicationContext(applicationContext)
            .registeredService(service)
            .build();
        assertFalse(strategy.authorizeRequest(accessRequest));
    }
}
