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
 * This is {@link GroovyRegisteredServiceSingleSignOnParticipationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("RegisteredService")
class GroovyRegisteredServiceSingleSignOnParticipationPolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(),
        "GroovyRegisteredServiceSingleSignOnParticipationPolicy.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyExternalGroovyFile() throws Throwable {
        val ticket = mock(AuthenticationAwareTicket.class);
        when(ticket.getAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication("Gandalf"));
        val results = new GroovyRegisteredServiceSingleSignOnParticipationPolicy();
        results.setGroovyScript("classpath:GroovySSOParticipationPolicy.groovy");
        assertTrue(results.shouldParticipateInSso(CoreAuthenticationTestUtils.getRegisteredService(), ticket));
    }

    @Test
    void verifyInlineGroovyFile() throws Throwable {
        val ticket = mock(AuthenticationAwareTicket.class);
        when(ticket.getAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication("Frodo"));
        val results = new GroovyRegisteredServiceSingleSignOnParticipationPolicy();
        results.setGroovyScript("groovy { authentication.principal.id == 'Frodo' }");
        assertTrue(results.shouldParticipateInSso(CoreAuthenticationTestUtils.getRegisteredService(), ticket));
    }

    @Test
    void verifySerializeToJson() throws Throwable {
        val strategy = new GroovyRegisteredServiceSingleSignOnParticipationPolicy();
        strategy.setGroovyScript("groovy { return false }");
        MAPPER.writeValue(JSON_FILE, strategy);
        val policyRead = MAPPER.readValue(JSON_FILE, GroovyRegisteredServiceSingleSignOnParticipationPolicy.class);
        assertEquals(strategy, policyRead);
    }
}
