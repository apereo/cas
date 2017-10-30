package org.apereo.cas.ticket.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
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

    /**
     * Factory to create the principal type.
     **/
    protected PrincipalFactory principalFactory = new DefaultPrincipalFactory();

    private RememberMeDelegatingExpirationPolicy p;

    @Before
    public void setUp() {
        final MultiTimeUseOrTimeoutExpirationPolicy rememberMe = new MultiTimeUseOrTimeoutExpirationPolicy(1, 20000);
        p = new RememberMeDelegatingExpirationPolicy(rememberMe);
        p.addPolicy(RememberMeDelegatingExpirationPolicy.PolicyTypes.REMEMBER_ME, rememberMe);
        p.addPolicy(RememberMeDelegatingExpirationPolicy.PolicyTypes.DEFAULT,
                new MultiTimeUseOrTimeoutExpirationPolicy(5, 20000));
    }

    @Test
    public void verifyTicketExpirationWithRememberMe() {
        final Authentication authentication = CoreAuthenticationTestUtils.getAuthentication(
                this.principalFactory.createPrincipal("test"),
                Collections.singletonMap(
                        RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, true));
        final TicketGrantingTicketImpl t = new TicketGrantingTicketImpl("111", authentication, this.p);
        assertFalse(t.isExpired());
        t.grantServiceTicket("55", RegisteredServiceTestUtils.getService(), this.p, false, true);
        assertTrue(t.isExpired());
    }

    @Test
    public void verifyTicketExpirationWithoutRememberMe() {
        final Authentication authentication = CoreAuthenticationTestUtils.getAuthentication();
        final TicketGrantingTicketImpl t = new TicketGrantingTicketImpl("111", authentication, this.p);
        assertFalse(t.isExpired());
        t.grantServiceTicket("55", RegisteredServiceTestUtils.getService(), this.p, false, true);
        assertFalse(t.isExpired());
    }

    @Test
    public void verifySerializeATimeoutExpirationPolicyToJson() throws IOException {
        MAPPER.writeValue(JSON_FILE, p);
        final ExpirationPolicy policyRead = MAPPER.readValue(JSON_FILE, RememberMeDelegatingExpirationPolicy.class);
        assertEquals(p, policyRead);
    }
}
