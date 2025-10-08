package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import tools.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link StaticRegisteredServiceUsernameProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("RegisteredService")
class StaticRegisteredServiceUsernameProviderTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyOperation() throws Throwable {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        
        System.setProperty("CAS_UID", "casuser");
        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAuthenticationTestUtils.getPrincipal("casuser"))
            .applicationContext(applicationContext)
            .build();
        val provider = new StaticRegisteredServiceUsernameProvider();
        provider.setValue("${#systemProperties['CAS_UID']}");
        assertEquals("casuser", provider.resolveUsername(usernameContext));

        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        MAPPER.writeValue(jsonFile, provider);
        val policyRead = MAPPER.readValue(jsonFile, StaticRegisteredServiceUsernameProvider.class);
        assertEquals(provider, policyRead);
    }
}
