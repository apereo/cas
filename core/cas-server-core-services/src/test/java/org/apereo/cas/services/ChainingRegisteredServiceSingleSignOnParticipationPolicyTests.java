package org.apereo.cas.services;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ChainingRegisteredServiceSingleSignOnParticipationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("RegisteredService")
class ChainingRegisteredServiceSingleSignOnParticipationPolicyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyOperation() throws Throwable {
        val input = mock(RegisteredServiceSingleSignOnParticipationPolicy.class);
        when(input.getOrder()).thenCallRealMethod();
        when(input.getCreateCookieOnRenewedAuthentication()).thenCallRealMethod();
        assertEquals(0, input.getOrder());
        assertEquals(TriStateBoolean.UNDEFINED, input.getCreateCookieOnRenewedAuthentication());
    }

    @Test
    void verifySsoParticipationByAuthenticationDateFails() throws Throwable {
        val authn = mock(Authentication.class);
        when(authn.getAuthenticationDate()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(10));

        val state = mock(AuthenticationAwareTicket.class);
        when(state.getAuthentication()).thenReturn(authn);
        val chain = new ChainingRegisteredServiceSingleSignOnParticipationPolicy();
        chain.addPolicy(new AuthenticationDateRegisteredServiceSingleSignOnParticipationPolicy(TimeUnit.SECONDS, 1, 0));
        assertFalse(chain.shouldParticipateInSso(RegisteredServiceTestUtils.getRegisteredService(), state));
    }

    @Test
    void verifySsoParticipationByAuthnAttribute() throws Throwable {
        val state = mock(AuthenticationAwareTicket.class);
        when(state.getAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication(Map.of("tag", List.of("123-abc"))));
        val chain = new ChainingRegisteredServiceSingleSignOnParticipationPolicy();
        val policy = new AttributeBasedRegisteredServiceSingleSignOnParticipationPolicy();
        policy.setAttributes(Map.of("tag", List.of("123-.*")));
        chain.addPolicy(policy);
        assertTrue(chain.shouldParticipateInSso(RegisteredServiceTestUtils.getRegisteredService(), state));
    }

    @Test
    void verifySsoParticipationByPrincipalAttribute() throws Throwable {
        val state = mock(AuthenticationAwareTicket.class);
        when(state.getAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication("casuser", Map.of("cn", List.of("1/2/3"))));
        val chain = new ChainingRegisteredServiceSingleSignOnParticipationPolicy();
        val policy = new AttributeBasedRegisteredServiceSingleSignOnParticipationPolicy();
        policy.setAttributes(Map.of("cn", List.of("\\d/\\d/\\d")));
        chain.addPolicy(policy);
        assertTrue(chain.shouldParticipateInSso(RegisteredServiceTestUtils.getRegisteredService(), state));
    }

    @Test
    void verifySsoParticipationByAttributeAsJson() throws IOException {
        val policy = new AttributeBasedRegisteredServiceSingleSignOnParticipationPolicy();
        policy.setAttributes(Map.of("cn", List.of("\\d/\\d/\\d")));
        policy.setRequireAllAttributes(true);
        val file = Files.createTempFile("attr", ".json").toFile();
        MAPPER.writeValue(file, policy);
        val read = MAPPER.readValue(file, AttributeBasedRegisteredServiceSingleSignOnParticipationPolicy.class);
        assertEquals(policy, read);
    }

    @Test
    void verifySsoParticipationAllAttributes() throws Throwable {
        val state = mock(AuthenticationAwareTicket.class);
        when(state.getAuthentication()).thenReturn(
            CoreAuthenticationTestUtils.getAuthentication("casuser", Map.of("cn", List.of("1/2/3"))));
        val chain = new ChainingRegisteredServiceSingleSignOnParticipationPolicy();
        val policy = new AttributeBasedRegisteredServiceSingleSignOnParticipationPolicy();
        policy.setAttributes(Map.of("cn", List.of("\\d/\\d/\\d"), "attr2", List.of("absent")));
        policy.setRequireAllAttributes(true);
        chain.addPolicy(policy);
        assertFalse(chain.shouldParticipateInSso(RegisteredServiceTestUtils.getRegisteredService(), state));
    }


    @Test
    void verifySsoParticipationByAuthenticationDatePasses() throws Throwable {
        val authn = mock(Authentication.class);
        when(authn.getAuthenticationDate()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(5));

        val state = mock(AuthenticationAwareTicket.class);
        when(state.getAuthentication()).thenReturn(authn);
        val chain = new ChainingRegisteredServiceSingleSignOnParticipationPolicy();
        chain.addPolicy(new AuthenticationDateRegisteredServiceSingleSignOnParticipationPolicy(TimeUnit.SECONDS, 10, 0));

        assertTrue(chain.shouldParticipateInSso(RegisteredServiceTestUtils.getRegisteredService(), state));
    }

    @Test
    void verifySsoParticipationByLastUsedTimeFails() throws Throwable {
        val state = mock(AuthenticationAwareTicket.class);
        when(state.getLastTimeUsed()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(10));
        val chain = new ChainingRegisteredServiceSingleSignOnParticipationPolicy();
        chain.addPolicy(new LastUsedTimeRegisteredServiceSingleSignOnParticipationPolicy(TimeUnit.SECONDS, 1, 0));
        assertFalse(chain.shouldParticipateInSso(RegisteredServiceTestUtils.getRegisteredService(), state));
    }

    @Test
    void verifySsoParticipationByLastUsedTimePasses() throws Throwable {
        val state = mock(AuthenticationAwareTicket.class);
        when(state.getLastTimeUsed()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(5));
        val chain = new ChainingRegisteredServiceSingleSignOnParticipationPolicy();
        chain.addPolicy(new LastUsedTimeRegisteredServiceSingleSignOnParticipationPolicy(TimeUnit.SECONDS, 10, 0));

        assertTrue(chain.shouldParticipateInSso(RegisteredServiceTestUtils.getRegisteredService(), state));
    }

    @Test
    void verifyPolicies() throws Throwable {
        val chain = new ChainingRegisteredServiceSingleSignOnParticipationPolicy();
        chain.addPolicies(new LastUsedTimeRegisteredServiceSingleSignOnParticipationPolicy(TimeUnit.SECONDS, 10, 0));
        assertFalse(chain.getPolicies().isEmpty());
        assertEquals(TriStateBoolean.TRUE, chain.getCreateCookieOnRenewedAuthentication());
    }
}
