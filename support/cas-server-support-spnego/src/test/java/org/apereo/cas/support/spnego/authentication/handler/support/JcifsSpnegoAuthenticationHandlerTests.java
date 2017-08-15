package org.apereo.cas.support.spnego.authentication.handler.support;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.support.spnego.MockJcifsAuthentication;
import org.apereo.cas.support.spnego.MockUnsuccessfulJcifsAuthentication;
import org.apereo.cas.support.spnego.authentication.principal.SpnegoCredential;
import org.junit.Test;

import java.security.GeneralSecurityException;

import static org.junit.Assert.*;

/**
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @since 4.2.0
 */
public class JcifsSpnegoAuthenticationHandlerTests {

    private static final String USERNAME = "Username";

    @Test
    public void verifySuccessfulAuthenticationWithDomainName() throws Exception {
        final SpnegoCredential credentials = new SpnegoCredential(new byte[] {0, 1, 2});
        final AuthenticationHandler authenticationHandler = new JcifsSpnegoAuthenticationHandler("", null, null, new MockJcifsAuthentication(), true, true);
        assertNotNull(authenticationHandler.authenticate(credentials));
        assertEquals("test", credentials.getPrincipal().getId());
        assertNotNull(credentials.getNextToken());
    }

    @Test
    public void verifySuccessfulAuthenticationWithoutDomainName() throws Exception {
        final SpnegoCredential credentials = new SpnegoCredential(new byte[] {0, 1, 2});
        final AuthenticationHandler authenticationHandler = new JcifsSpnegoAuthenticationHandler("", null, null, new MockJcifsAuthentication(), false, true);
        assertNotNull(authenticationHandler.authenticate(credentials));
        assertEquals("test", credentials.getPrincipal().getId());
        assertNotNull(credentials.getNextToken());
    }

    @Test
    public void verifyUnsuccessfulAuthenticationWithExceptionOnProcess() throws Exception {
        final SpnegoCredential credentials = new SpnegoCredential(new byte[] {0, 1, 2});
        final AuthenticationHandler authenticationHandler = new JcifsSpnegoAuthenticationHandler("", null, null, new MockUnsuccessfulJcifsAuthentication(true),
                true, true);

        try {
            authenticationHandler.authenticate(credentials);
            fail("An AbstractAuthenticationException should have been thrown");
        } catch (final GeneralSecurityException e) {
            assertNull(credentials.getNextToken());
            assertNull(credentials.getPrincipal());
        }
    }

    @Test
    public void verifyUnsuccessfulAuthentication() throws Exception {
        final SpnegoCredential credentials = new SpnegoCredential(new byte[] {0, 1, 2});
        final AuthenticationHandler authenticationHandler = new JcifsSpnegoAuthenticationHandler("", null, null, new MockUnsuccessfulJcifsAuthentication(false),
                true, true);

        try {
            authenticationHandler.authenticate(credentials);
            fail("An AbstractAuthenticationException should have been thrown");
        } catch (final GeneralSecurityException e) {
            assertNull(credentials.getNextToken());
            assertNull(credentials.getPrincipal());
        }
    }

    @Test
    public void verifySupports() {
        final AuthenticationHandler authenticationHandler = new JcifsSpnegoAuthenticationHandler("", null, null, new MockJcifsAuthentication(), true, true);

        assertFalse(authenticationHandler.supports(null));
        assertTrue(authenticationHandler.supports(new SpnegoCredential(new byte[] {0, 1, 2})));
        assertFalse(authenticationHandler.supports(new UsernamePasswordCredential()));
    }

    @Test
    public void verifyGetSimpleCredentials() {
        final String myNtlmUser = "DOMAIN\\Username";
        final String myNtlmUserWithNoDomain = USERNAME;
        final String myKerberosUser = "Username@DOMAIN.COM";

        final PrincipalFactory factory = new DefaultPrincipalFactory();
        final JcifsSpnegoAuthenticationHandler authenticationHandler = new JcifsSpnegoAuthenticationHandler("", null, null, new MockJcifsAuthentication(), true,
                true);

        assertEquals(factory.createPrincipal(myNtlmUser), authenticationHandler.getPrincipal(myNtlmUser, true));
        assertEquals(factory.createPrincipal(myNtlmUserWithNoDomain), authenticationHandler.getPrincipal(myNtlmUserWithNoDomain, false));
        assertEquals(factory.createPrincipal(myKerberosUser), authenticationHandler.getPrincipal(myKerberosUser, false));

        final JcifsSpnegoAuthenticationHandler handlerNoDomain = new JcifsSpnegoAuthenticationHandler("", null, null, new MockJcifsAuthentication(), false,
                true);
        assertEquals(factory.createPrincipal(USERNAME), handlerNoDomain.getPrincipal(myNtlmUser, true));
        assertEquals(factory.createPrincipal(USERNAME), handlerNoDomain.getPrincipal(myNtlmUserWithNoDomain, true));
        assertEquals(factory.createPrincipal(USERNAME), handlerNoDomain.getPrincipal(myKerberosUser, false));
    }
}
