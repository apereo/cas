package org.apereo.cas.ticket.expiration;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationResultBuilder;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.authentication.principal.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.SerializationUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RememberMeDelegatingExpirationPolicy.
 *
 * @author Scott Battaglia
 * @since 3.2.1
 */
@Tag("Simple")
public class RememberMeDelegatingExpirationPolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "rememberMeDelegatingExpirationPolicy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private static final Long REMEMBER_ME_TTL = 20000L;
    private static final Long DEFAULT_TTL = 10000L;

    /**
     * Factory to create the principal type.
     **/
    protected PrincipalFactory principalFactory = PrincipalFactoryUtils.newPrincipalFactory();

    private RememberMeDelegatingExpirationPolicy expirationPolicy;

    @BeforeEach
    public void initialize() {
        val rememberMe = new MultiTimeUseOrTimeoutExpirationPolicy(1, REMEMBER_ME_TTL);
        expirationPolicy = new RememberMeDelegatingExpirationPolicy();
        expirationPolicy.addPolicy(RememberMeDelegatingExpirationPolicy.POLICY_NAME_REMEMBER_ME, rememberMe);
        expirationPolicy.addPolicy(RememberMeDelegatingExpirationPolicy.POLICY_NAME_DEFAULT,
            new MultiTimeUseOrTimeoutExpirationPolicy(5, DEFAULT_TTL));
    }

    @Test
    public void verifyTicketExpirationWithRememberMe() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication(
            this.principalFactory.createPrincipal("test"),
            Collections.singletonMap(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, List.of(true)));
        val t = new TicketGrantingTicketImpl("111", authentication, this.expirationPolicy);
        assertFalse(t.isExpired());
        t.grantServiceTicket("55", RegisteredServiceTestUtils.getService(), this.expirationPolicy, false, true);
        assertTrue(t.isExpired());
    }

    @Test
    public void verifyTicketExpirationWithRememberMeBuiltAuthn() {
        val builder = new DefaultAuthenticationResultBuilder();
        val p1 = CoreAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("uid", "casuser"));
        val authn1 = CoreAuthenticationTestUtils.getAuthentication(p1, CollectionUtils.wrap(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, true));
        val result = builder.collect(authn1).build(new DefaultPrincipalElectionStrategy());

        val authentication = result.getAuthentication();
        assertNotNull(authentication);

        val t = new TicketGrantingTicketImpl("111", authentication, this.expirationPolicy);
        assertFalse(t.isExpired());
        t.grantServiceTicket("55", RegisteredServiceTestUtils.getService(), this.expirationPolicy, false, true);
        assertTrue(t.isExpired());
    }

    @Test
    public void verifyTicketExpirationWithoutRememberMe() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        val t = new TicketGrantingTicketImpl("111", authentication, this.expirationPolicy);
        assertFalse(t.isExpired());
        t.grantServiceTicket("55", RegisteredServiceTestUtils.getService(), this.expirationPolicy, false, true);
        assertFalse(t.isExpired());
    }

    @Test
    public void verifyTicketTTLWithRememberMe() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication(
            this.principalFactory.createPrincipal("test"),
            Collections.singletonMap(
                RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, List.of(true)));
        val t = new TicketGrantingTicketImpl("111", authentication, this.expirationPolicy);
        assertEquals(REMEMBER_ME_TTL, expirationPolicy.getTimeToLive(t));
    }

    @Test
    public void verifyTicketTTLWithoutRememberMe() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        val t = new TicketGrantingTicketImpl("111", authentication, this.expirationPolicy);
        assertEquals(DEFAULT_TTL, expirationPolicy.getTimeToLive(t));
    }

    @Test
    public void verifySerializeATimeoutExpirationPolicyToJson() throws IOException {
        MAPPER.writeValue(JSON_FILE, expirationPolicy);
        val policyRead = MAPPER.readValue(JSON_FILE, RememberMeDelegatingExpirationPolicy.class);
        assertEquals(expirationPolicy, policyRead);
    }

    @Test
    public void verifySerialization() {
        val result = SerializationUtils.serialize(expirationPolicy);
        val policyRead = SerializationUtils.deserialize(result, RememberMeDelegatingExpirationPolicy.class);
        assertEquals(expirationPolicy, policyRead);
    }
}
