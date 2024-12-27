package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GroovyRegisteredServiceSingleSignOnParticipationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("RegisteredService")
class GroovyRegisteredServiceSingleSignOnParticipationPolicyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyExternalGroovyFile() {
        val ticket = mock(AuthenticationAwareTicket.class);
        when(ticket.getAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication("Gandalf"));
        val results = new GroovyRegisteredServiceSingleSignOnParticipationPolicy();
        results.setGroovyScript("classpath:GroovySSOParticipationPolicy.groovy");
        assertTrue(results.shouldParticipateInSso(CoreAuthenticationTestUtils.getRegisteredService(), ticket));
    }

    @Test
    void verifyInlineGroovyFile() {
        val ticket = mock(AuthenticationAwareTicket.class);
        when(ticket.getAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication("Frodo"));
        val results = new GroovyRegisteredServiceSingleSignOnParticipationPolicy();
        results.setGroovyScript("groovy { authentication.principal.id == 'Frodo' }");
        assertTrue(results.shouldParticipateInSso(CoreAuthenticationTestUtils.getRegisteredService(), ticket));
    }

    @Test
    void verifySerializeToJson() throws Throwable {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val strategy = new GroovyRegisteredServiceSingleSignOnParticipationPolicy();
        strategy.setGroovyScript("groovy { return false }");
        MAPPER.writeValue(jsonFile, strategy);
        val policyRead = MAPPER.readValue(jsonFile, GroovyRegisteredServiceSingleSignOnParticipationPolicy.class);
        assertEquals(strategy, policyRead);
    }
}
