package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyRegisteredServiceAccessStrategyActivationCriteriaTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("RegisteredService")
public class GroovyRegisteredServiceAccessStrategyActivationCriteriaTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(),
        "ChainingRegisteredServiceAccessStrategyActivationCriteriaTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    public void verifyExternalGroovyFile() {
        val request = RegisteredServiceAccessStrategyRequest.builder().principalId("casuser")
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .attributes(CollectionUtils.wrap("key1", Set.of("value1"))).build();
        val results = new GroovyRegisteredServiceAccessStrategyActivationCriteria();
        results.setGroovyScript("classpath:GroovyAccessActivation.groovy");
        assertTrue(results.shouldActivate(request));
    }

    @Test
    public void verifyInlineGroovyFile() {
        val request = RegisteredServiceAccessStrategyRequest.builder().principalId("casuser")
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .attributes(CollectionUtils.wrap("key1", Set.of("value1"))).build();
        val results = new GroovyRegisteredServiceAccessStrategyActivationCriteria();
        results.setGroovyScript("groovy { assert accessRequest != null; return false }");
        assertFalse(results.shouldActivate(request));
    }

    @Test
    public void verifySerializeToJson() throws Exception {
        val strategy = new GroovyRegisteredServiceAccessStrategyActivationCriteria();
        strategy.setGroovyScript("groovy { assert accessRequest != null; return false }");
        MAPPER.writeValue(JSON_FILE, strategy);
        val policyRead = MAPPER.readValue(JSON_FILE, RegisteredServiceAccessStrategyActivationCriteria.class);
        assertEquals(strategy, policyRead);
    }
}
