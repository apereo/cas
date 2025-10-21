package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyRegisteredServiceAttributeReleaseActivationCriteriaTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("RegisteredService")
class GroovyRegisteredServiceAttributeReleaseActivationCriteriaTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyExternalGroovyFile() {
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .principal(CoreAuthenticationTestUtils.getPrincipal("Gandalf"))
            .build();
        val results = new GroovyRegisteredServiceAttributeReleaseActivationCriteria();
        results.setGroovyScript("classpath:GroovyAttributeReleaseCriteria.groovy");
        assertTrue(results.shouldActivate(context));
    }

    @Test
    void verifyInlineGroovyFile() {
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .principal(CoreAuthenticationTestUtils.getPrincipal("Frodo"))
            .build();
        val results = new GroovyRegisteredServiceAttributeReleaseActivationCriteria();
        results.setGroovyScript("groovy { context.principal.id == 'Frodo' }");
        assertTrue(results.shouldActivate(context));
    }

    @Test
    void verifySerializeToJson() throws Throwable {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val strategy = new GroovyRegisteredServiceAttributeReleaseActivationCriteria();
        strategy.setGroovyScript("groovy { return false }");
        MAPPER.writeValue(jsonFile, strategy);
        val policyRead = MAPPER.readValue(jsonFile, GroovyRegisteredServiceAttributeReleaseActivationCriteria.class);
        assertEquals(strategy, policyRead);
    }
}
