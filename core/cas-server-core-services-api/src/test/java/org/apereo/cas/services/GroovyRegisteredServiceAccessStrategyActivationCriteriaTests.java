package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyRegisteredServiceAccessStrategyActivationCriteriaTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("RegisteredService")
class GroovyRegisteredServiceAccessStrategyActivationCriteriaTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyExternalGroovyFile() throws Throwable {
        val request = RegisteredServiceAccessStrategyRequest.builder().principalId("casuser")
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .attributes(CollectionUtils.wrap("key1", Set.of("value1"))).build();
        val results = new GroovyRegisteredServiceAccessStrategyActivationCriteria();
        results.setGroovyScript("classpath:GroovyAccessActivation.groovy");
        assertTrue(results.shouldActivate(request));
    }

    @Test
    void verifyInlineGroovyFile() throws Throwable {
        val request = RegisteredServiceAccessStrategyRequest.builder().principalId("casuser")
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .attributes(CollectionUtils.wrap("key1", Set.of("value1"))).build();
        val results = new GroovyRegisteredServiceAccessStrategyActivationCriteria();
        results.setGroovyScript("groovy { assert accessRequest != null; return false }");
        assertFalse(results.shouldActivate(request));
    }

    @Test
    void verifySerializeToJson() throws Throwable {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val strategy = new GroovyRegisteredServiceAccessStrategyActivationCriteria();
        strategy.setGroovyScript("groovy { assert accessRequest != null; return false }");
        MAPPER.writeValue(jsonFile, strategy);
        val policyRead = MAPPER.readValue(jsonFile, RegisteredServiceAccessStrategyActivationCriteria.class);
        assertEquals(strategy, policyRead);
    }
}
