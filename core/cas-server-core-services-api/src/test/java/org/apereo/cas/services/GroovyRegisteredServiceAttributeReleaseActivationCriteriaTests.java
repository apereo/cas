package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GroovyRegisteredServiceAttributeReleaseActivationCriteriaTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("RegisteredService")
public class GroovyRegisteredServiceAttributeReleaseActivationCriteriaTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(),
        "GroovyRegisteredServiceAttributeReleaseActivationCriteria.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyExternalGroovyFile() throws Throwable {
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .principal(CoreAuthenticationTestUtils.getPrincipal("Gandalf"))
            .build();
        val results = new GroovyRegisteredServiceAttributeReleaseActivationCriteria();
        results.setGroovyScript("classpath:GroovyAttributeReleaseCriteria.groovy");
        assertTrue(results.shouldActivate(context));
    }

    @Test
    void verifyInlineGroovyFile() throws Throwable {
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .principal(CoreAuthenticationTestUtils.getPrincipal("Frodo"))
            .build();
        val results = new GroovyRegisteredServiceAttributeReleaseActivationCriteria();
        results.setGroovyScript("groovy { context.principal.id == 'Frodo' }");
        assertTrue(results.shouldActivate(context));
    }

    @Test
    void verifySerializeToJson() throws Throwable {
        val strategy = new GroovyRegisteredServiceAttributeReleaseActivationCriteria();
        strategy.setGroovyScript("groovy { return false }");
        MAPPER.writeValue(JSON_FILE, strategy);
        val policyRead = MAPPER.readValue(JSON_FILE, GroovyRegisteredServiceAttributeReleaseActivationCriteria.class);
        assertEquals(strategy, policyRead);
    }
}
