package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is test cases for
 * {@link GroovyRegisteredServiceAccessStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("GroovyServices")
@ExtendWith(CasTestExtension.class)
class GroovyRegisteredServiceAccessStrategyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "GroovyRegisteredServiceAccessStrategyTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void checkDefaultAuthzStrategyConfig() {
        val accessStrategy = new GroovyRegisteredServiceAccessStrategy();
        accessStrategy.setGroovyScript("classpath:GroovyServiceAccessStrategy.groovy");
        assertTrue(accessStrategy.isServiceAccessAllowed(RegisteredServiceTestUtils.getRegisteredService(), CoreAuthenticationTestUtils.getService()));
        assertTrue(accessStrategy.isServiceAccessAllowedForSso(RegisteredServiceTestUtils.getRegisteredService()));
        val request = RegisteredServiceAccessStrategyRequest.builder()
            .service(RegisteredServiceTestUtils.getService2())
            .principalId(UUID.randomUUID().toString())
            .build();
        assertTrue(accessStrategy.authorizeRequest(request));
        assertNull(accessStrategy.getUnauthorizedRedirectUrl());
        assertNotNull(accessStrategy.getDelegatedAuthenticationPolicy());
        assertNotNull(accessStrategy.getRequiredAttributes());
    }

    @Test
    void verifyFailingOps() {
        val accessStrategy = new GroovyRegisteredServiceAccessStrategy();
        accessStrategy.setGroovyScript("classpath:Unknown.groovy");
        assertThrows(UnauthorizedServiceException.class, () -> accessStrategy.isServiceAccessAllowed(null, null));
        assertThrows(UnauthorizedServiceException.class, () -> accessStrategy.isServiceAccessAllowedForSso(null));
        assertThrows(UnauthorizedServiceException.class, () -> accessStrategy.authorizeRequest(null));
    }

    @Test
    void verifySerializationToJson() throws IOException {
        val accessStrategy = new GroovyRegisteredServiceAccessStrategy();
        accessStrategy.setGroovyScript("classpath:GroovyServiceAccessStrategy.groovy");
        MAPPER.writeValue(JSON_FILE, accessStrategy);
        val strategyRead = MAPPER.readValue(JSON_FILE, GroovyRegisteredServiceAccessStrategy.class);
        assertEquals(accessStrategy, strategyRead);
    }
}
