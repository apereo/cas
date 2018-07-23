package org.apereo.cas.support.spnego.authentication.handler.support;

import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.spnego.MockJcifsAuthentication;
import org.apereo.cas.support.spnego.MockUnsuccessfulJcifsAuthentication;
import org.apereo.cas.support.spnego.authentication.principal.SpnegoCredential;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.security.GeneralSecurityException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @since 4.2.0
 */
public class JcifsSpnegoAuthenticationHandlerTests {

    private static final String USERNAME = "Username";

    @Test
    public void verifySuccessfulAuthenticationWithDomainName() throws Exception {
        val credentials = new SpnegoCredential(new byte[]{0, 1, 2});
        val authenticationHandler = new JcifsSpnegoAuthenticationHandler("", null, null, new MockJcifsAuthentication(), true, true);
        assertNotNull(authenticationHandler.authenticate(credentials));
        assertEquals("test", credentials.getPrincipal().getId());
        assertNotNull(credentials.getNextToken());
    }

    @Test
    public void verifySuccessfulAuthenticationWithoutDomainName() throws Exception {
        val credentials = new SpnegoCredential(new byte[]{0, 1, 2});
        val authenticationHandler = new JcifsSpnegoAuthenticationHandler("", null, null, new MockJcifsAuthentication(), false, true);
        assertNotNull(authenticationHandler.authenticate(credentials));
        assertEquals("test", credentials.getPrincipal().getId());
        assertNotNull(credentials.getNextToken());
    }

    @Test
    public void verifyUnsuccessfulAuthenticationWithExceptionOnProcess() throws Exception {
        val credentials = new SpnegoCredential(new byte[]{0, 1, 2});
        val authenticationHandler = new JcifsSpnegoAuthenticationHandler("", null, null, new MockUnsuccessfulJcifsAuthentication(true),
            true, true);

        try {
            authenticationHandler.authenticate(credentials);
            throw new AssertionError("An AbstractAuthenticationException should have been thrown");
        } catch (final GeneralSecurityException e) {
            assertNull(credentials.getNextToken());
            assertNull(credentials.getPrincipal());
        }
    }

    @Test
    public void verifyUnsuccessfulAuthentication() throws Exception {
        val credentials = new SpnegoCredential(new byte[]{0, 1, 2});
        val authenticationHandler = new JcifsSpnegoAuthenticationHandler("", null, null, new MockUnsuccessfulJcifsAuthentication(false),
            true, true);

        try {
            authenticationHandler.authenticate(credentials);
            throw new AssertionError("An AbstractAuthenticationException should have been thrown");
        } catch (final GeneralSecurityException e) {
            assertNull(credentials.getNextToken());
            assertNull(credentials.getPrincipal());
        }
    }

    @Test
    public void verifySupports() {
        val authenticationHandler = new JcifsSpnegoAuthenticationHandler("", null, null, new MockJcifsAuthentication(), true, true);

        assertFalse(authenticationHandler.supports(null));
        assertTrue(authenticationHandler.supports(new SpnegoCredential(new byte[]{0, 1, 2})));
        assertFalse(authenticationHandler.supports(new UsernamePasswordCredential()));
    }

    @Test
    public void verifyGetSimpleCredentials() {
        val myNtlmUser = "DOMAIN\\Username";
        val myNtlmUserWithNoDomain = USERNAME;
        val myKerberosUser = "Username@DOMAIN.COM";

        val factory = new DefaultPrincipalFactory();
        val authenticationHandler = new JcifsSpnegoAuthenticationHandler("", null, null, new MockJcifsAuthentication(), true,
            true);

        assertEquals(factory.createPrincipal(myNtlmUser), authenticationHandler.getPrincipal(myNtlmUser, true));
        assertEquals(factory.createPrincipal(myNtlmUserWithNoDomain), authenticationHandler.getPrincipal(myNtlmUserWithNoDomain, false));
        assertEquals(factory.createPrincipal(myKerberosUser), authenticationHandler.getPrincipal(myKerberosUser, false));

        val handlerNoDomain =
            new JcifsSpnegoAuthenticationHandler(StringUtils.EMPTY, mock(ServicesManager.class),
                new DefaultPrincipalFactory(), new MockJcifsAuthentication(), false, true);
        assertEquals(factory.createPrincipal(USERNAME), handlerNoDomain.getPrincipal(myNtlmUser, true));
        assertEquals(factory.createPrincipal(USERNAME), handlerNoDomain.getPrincipal(myNtlmUserWithNoDomain, true));
        assertEquals(factory.createPrincipal(USERNAME), handlerNoDomain.getPrincipal(myKerberosUser, false));
    }
}
