package org.jasig.cas.support.spnego.authentication.handler.support;

import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.principal.PrincipalFactory;
import org.jasig.cas.support.spnego.MockJcifsAuthentication;
import org.jasig.cas.support.spnego.authentication.principal.SpnegoCredential;
import org.junit.Before;
import org.junit.Test;

import java.security.GeneralSecurityException;

import static org.junit.Assert.*;

/**
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @since 4.2.0
 */
public class JcifsSpnegoAuthenticationHandlerTests {
    private JcifsSpnegoAuthenticationHandler authenticationHandler;

    @Before
    public void setUp() throws Exception {
        this.authenticationHandler = new JcifsSpnegoAuthenticationHandler();
    }

    @Test
    public void verifySuccessfulAuthenticationWithDomainName() throws Exception {
        final SpnegoCredential credentials = new SpnegoCredential(new byte[] {0, 1, 2});
        this.authenticationHandler.setPrincipalWithDomainName(true);
        this.authenticationHandler.setAuthentication(new MockJcifsAuthentication(true));
        assertNotNull(this.authenticationHandler.authenticate(credentials));
        assertEquals("test", credentials.getPrincipal().getId());
        assertNotNull(credentials.getNextToken());
    }

    @Test
    public void verifySuccessfulAuthenticationWithoutDomainName() throws Exception {
        final SpnegoCredential credentials = new SpnegoCredential(new byte[] {0, 1, 2});
        this.authenticationHandler.setPrincipalWithDomainName(false);
        this.authenticationHandler.setAuthentication(new MockJcifsAuthentication(true));
        assertNotNull(this.authenticationHandler.authenticate(credentials));
        assertEquals("test", credentials.getPrincipal().getId());
        assertNotNull(credentials.getNextToken());
    }

    @Test
    public void verifyUnsuccessfulAuthentication() throws Exception {
        final SpnegoCredential credentials = new SpnegoCredential(new byte[] {0, 1, 2});
        this.authenticationHandler.setAuthentication(new MockJcifsAuthentication(false));
        try {
            this.authenticationHandler.authenticate(credentials);
            fail("An AbstractAuthenticationException should have been thrown");
        } catch (final GeneralSecurityException e) {
            assertNull(credentials.getNextToken());
            assertNull(credentials.getPrincipal());
        }
    }

    @Test
    public void verifySupports() {
        assertFalse(this.authenticationHandler.supports(null));
        assertTrue(this.authenticationHandler.supports(new SpnegoCredential(new byte[] {0, 1, 2})));
        assertFalse(this.authenticationHandler.supports(new UsernamePasswordCredential()));
    }

    @Test
    public void verifyGetSimpleCredentials() {
        final String myNtlmUser = "DOMAIN\\Username";
        final String myNtlmUserWithNoDomain = "Username";
        final String myKerberosUser = "Username@DOMAIN.COM";

        final PrincipalFactory factory = new DefaultPrincipalFactory();

        this.authenticationHandler.setPrincipalWithDomainName(true);
        assertEquals(factory.createPrincipal(myNtlmUser), this.authenticationHandler
                .getPrincipal(myNtlmUser, true));
        assertEquals(factory.createPrincipal(myNtlmUserWithNoDomain), this.authenticationHandler
                .getPrincipal(myNtlmUserWithNoDomain, false));
        assertEquals(factory.createPrincipal(myKerberosUser), this.authenticationHandler
                .getPrincipal(myKerberosUser, false));

        this.authenticationHandler.setPrincipalWithDomainName(false);
        assertEquals(factory.createPrincipal("Username"), this.authenticationHandler
                .getPrincipal(myNtlmUser, true));
        assertEquals(factory.createPrincipal("Username"), this.authenticationHandler
                .getPrincipal(myNtlmUserWithNoDomain, true));
        assertEquals(factory.createPrincipal("Username"), this.authenticationHandler
                .getPrincipal(myKerberosUser, false));
    }
}
