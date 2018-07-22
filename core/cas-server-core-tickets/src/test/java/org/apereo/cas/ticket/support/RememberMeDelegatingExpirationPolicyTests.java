package org.apereo.cas.ticket.support;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Tests for RememberMeDelegatingExpirationPolicy.
 *
 * @author Scott Battaglia
 * @since 3.2.1
 */
public class RememberMeDelegatingExpirationPolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "rememberMeDelegatingExpirationPolicy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private static final Long REMEMBER_ME_TTL = 20000L;
    private static final Long DEFAULT_TTL = 10000L;

    /**
     * Factory to create the principal type.
     **/
    protected PrincipalFactory principalFactory = new DefaultPrincipalFactory();

    private RememberMeDelegatingExpirationPolicy p;

    @Before
    public void initialize() {
        val rememberMe = new MultiTimeUseOrTimeoutExpirationPolicy(1, REMEMBER_ME_TTL);
        p = new RememberMeDelegatingExpirationPolicy(rememberMe);
        p.addPolicy(RememberMeDelegatingExpirationPolicy.PolicyTypes.REMEMBER_ME, rememberMe);
        p.addPolicy(RememberMeDelegatingExpirationPolicy.PolicyTypes.DEFAULT,
            new MultiTimeUseOrTimeoutExpirationPolicy(5, DEFAULT_TTL));
    }

    @Test
    public void verifyTicketExpirationWithRememberMe() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication(
            this.principalFactory.createPrincipal("test"),
            Collections.singletonMap(
                RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, true));
        val t = new TicketGrantingTicketImpl("111", authentication, this.p);
        assertFalse(t.isExpired());
        t.grantServiceTicket("55", RegisteredServiceTestUtils.getService(), this.p, false, true);
        assertTrue(t.isExpired());
    }

    @Test
    public void verifyTicketExpirationWithoutRememberMe() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        val t = new TicketGrantingTicketImpl("111", authentication, this.p);
        assertFalse(t.isExpired());
        t.grantServiceTicket("55", RegisteredServiceTestUtils.getService(), this.p, false, true);
        assertFalse(t.isExpired());
    }

    @Test
    public void verifyTicketTTLWithRememberMe() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication(
            this.principalFactory.createPrincipal("test"),
            Collections.singletonMap(
                RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, true));
        val t = new TicketGrantingTicketImpl("111", authentication, this.p);
        assertEquals(REMEMBER_ME_TTL, p.getTimeToLive(t));
    }

    @Test
    public void verifyTicketTTLWithoutRememberMe() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        val t = new TicketGrantingTicketImpl("111", authentication, this.p);
        assertEquals(DEFAULT_TTL, p.getTimeToLive(t));
    }

    @Test
    public void verifySerializeATimeoutExpirationPolicyToJson() throws IOException {
        MAPPER.writeValue(JSON_FILE, p);
        val policyRead = MAPPER.readValue(JSON_FILE, RememberMeDelegatingExpirationPolicy.class);
        assertEquals(p, policyRead);
    }
}
