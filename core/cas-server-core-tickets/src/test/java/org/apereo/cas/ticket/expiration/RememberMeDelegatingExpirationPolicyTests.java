package org.apereo.cas.ticket.expiration;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationResultBuilder;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.authentication.principal.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.factory.BaseTicketFactoryTests;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.serialization.SerializationUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RememberMeDelegatingExpirationPolicy.
 *
 * @author Scott Battaglia
 * @since 3.2.1
 */
@Tag("ExpirationPolicy")
@TestPropertySource(properties = "cas.ticket.tgt.core.only-track-most-recent-session=true")
class RememberMeDelegatingExpirationPolicyTests extends BaseTicketFactoryTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private static final Long REMEMBER_ME_TTL = 20000L;

    private static final Long DEFAULT_TTL = 10000L;

    /**
     * Factory to create the principal type.
     **/
    protected PrincipalFactory principalFactory = PrincipalFactoryUtils.newPrincipalFactory();

    private RememberMeDelegatingExpirationPolicy expirationPolicy;

    @BeforeEach
    void initialize() {
        val rememberMe = new MultiTimeUseOrTimeoutExpirationPolicy(1, REMEMBER_ME_TTL);
        expirationPolicy = new RememberMeDelegatingExpirationPolicy();
        expirationPolicy.addPolicy(RememberMeDelegatingExpirationPolicy.POLICY_NAME_REMEMBER_ME, rememberMe);
        expirationPolicy.addPolicy(BaseDelegatingExpirationPolicy.POLICY_NAME_DEFAULT,
            new MultiTimeUseOrTimeoutExpirationPolicy(5, DEFAULT_TTL));
    }

    @Test
    void verifyTicketExpirationWithRememberMe() throws Throwable {
        val authentication = CoreAuthenticationTestUtils.getAuthentication(
            this.principalFactory.createPrincipal("test"),
            Map.of(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, List.of(true)));
        val t = new TicketGrantingTicketImpl("111", authentication, this.expirationPolicy);
        assertFalse(t.isExpired());
        t.grantServiceTicket("55", RegisteredServiceTestUtils.getService(),
            this.expirationPolicy, false, serviceTicketSessionTrackingPolicy);
        assertTrue(t.isExpired());
    }

    @Test
    void verifyNoRememberMe() throws Throwable {
        val authentication = CoreAuthenticationTestUtils.getAuthentication(
            principalFactory.createPrincipal("test"),
            Map.of(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, List.of(false)));
        val t = new TicketGrantingTicketImpl("111", authentication, this.expirationPolicy);
        assertFalse(t.isExpired());
    }

    @Test
    void verifyTicketExpirationWithRememberMeBuiltAuthn() throws Throwable {
        val builder = new DefaultAuthenticationResultBuilder(new DefaultPrincipalElectionStrategy());
        val p1 = CoreAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("uid", "casuser"));
        val authn1 = CoreAuthenticationTestUtils.getAuthentication(p1,
            CollectionUtils.wrap(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, true));
        val result = builder.collect(authn1).build();

        val authentication = result.getAuthentication();
        assertNotNull(authentication);

        val t = new TicketGrantingTicketImpl("111", authentication, this.expirationPolicy);
        assertFalse(t.isExpired());
        t.grantServiceTicket("55", RegisteredServiceTestUtils.getService(),
            this.expirationPolicy, false, serviceTicketSessionTrackingPolicy);
        assertTrue(t.isExpired());
    }

    @Test
    void verifyTicketExpirationWithoutRememberMe() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        val t = new TicketGrantingTicketImpl("111", authentication, this.expirationPolicy);
        assertFalse(t.isExpired());
        t.grantServiceTicket("55", RegisteredServiceTestUtils.getService(),
            this.expirationPolicy, false, serviceTicketSessionTrackingPolicy);
        assertFalse(t.isExpired());
    }

    @Test
    void verifyTicketTTLWithRememberMe() throws Throwable {
        val authentication = CoreAuthenticationTestUtils.getAuthentication(
            this.principalFactory.createPrincipal("test"),
            Map.of(
                RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, List.of(true)));
        val t = new TicketGrantingTicketImpl("111", authentication, this.expirationPolicy);
        assertEquals(REMEMBER_ME_TTL, expirationPolicy.getTimeToLive(t));
    }

    @Test
    void verifyTicketTTLWithoutRememberMe() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        val t = new TicketGrantingTicketImpl("111", authentication, this.expirationPolicy);
        assertEquals(DEFAULT_TTL, expirationPolicy.getTimeToLive(t));
    }

    @Test
    void verifySerializeATimeoutExpirationPolicyToJson() throws IOException {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        MAPPER.writeValue(jsonFile, expirationPolicy);
        val policyRead = MAPPER.readValue(jsonFile, RememberMeDelegatingExpirationPolicy.class);
        assertEquals(expirationPolicy, policyRead);
    }

    @Test
    void verifySerialization() {
        val result = SerializationUtils.serialize(expirationPolicy);
        val policyRead = SerializationUtils.deserialize(result, RememberMeDelegatingExpirationPolicy.class);
        assertEquals(expirationPolicy, policyRead);
    }
}
