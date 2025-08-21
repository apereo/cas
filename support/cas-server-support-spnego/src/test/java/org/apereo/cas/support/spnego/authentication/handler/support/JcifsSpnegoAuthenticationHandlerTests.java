package org.apereo.cas.support.spnego.authentication.handler.support;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.spnego.SpnegoProperties;
import org.apereo.cas.support.spnego.MockJcifsAuthentication;
import org.apereo.cas.support.spnego.MockUnsuccessfulJcifsAuthentication;
import org.apereo.cas.support.spnego.authentication.principal.SpnegoCredential;
import org.apereo.cas.util.CollectionUtils;

import jcifs.spnego.Authentication;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @since 4.2.0
 */
@Tag("Spnego")
class JcifsSpnegoAuthenticationHandlerTests {

    private static final String USERNAME = "Username";

    private static final int POOL_SIZE = 10;

    @Test
    void verifySuccessfulAuthenticationWithDomainName() throws Throwable {
        val credentials = new SpnegoCredential(new byte[]{0, 1, 2});
        val queue = new ArrayBlockingQueue<List<Authentication>>(POOL_SIZE);
        queue.add(CollectionUtils.wrapList(new MockJcifsAuthentication()));
        val authenticationHandler = new JcifsSpnegoAuthenticationHandler(getProperties(true, true),
            PrincipalFactoryUtils.newPrincipalFactory(), queue);
        assertNotNull(authenticationHandler.authenticate(credentials, mock(Service.class)));
        assertEquals("test", credentials.getPrincipal().getId());
        assertNotNull(credentials.getNextToken());
        assertTrue(authenticationHandler.supports(credentials.getClass()));
    }

    @Test
    void verifySuccessfulAuthenticationWithoutDomainName() throws Throwable {
        val credentials = new SpnegoCredential(new byte[]{0, 1, 2});
        val queue = new ArrayBlockingQueue<List<Authentication>>(POOL_SIZE);
        queue.add(CollectionUtils.wrapList(new MockJcifsAuthentication()));
        val authenticationHandler = new JcifsSpnegoAuthenticationHandler(getProperties(false, true), null, queue);
        assertNotNull(authenticationHandler.authenticate(credentials, mock(Service.class)));
        assertEquals("test", credentials.getPrincipal().getId());
        assertNotNull(credentials.getNextToken());
    }

    @Test
    void verifyUnsuccessfulAuthenticationWithExceptionOnProcess() {
        val credentials = new SpnegoCredential(new byte[]{0, 1, 2});
        val queue = new ArrayBlockingQueue<List<Authentication>>(POOL_SIZE);
        queue.add(CollectionUtils.wrapList(new MockUnsuccessfulJcifsAuthentication(true)));
        val authenticationHandler = new JcifsSpnegoAuthenticationHandler(getProperties(true, true), null, queue);

        authenticate(credentials, authenticationHandler);
    }

    private static void authenticate(final SpnegoCredential credentials, final JcifsSpnegoAuthenticationHandler authenticationHandler) {
        try {
            authenticationHandler.authenticate(credentials, mock(Service.class));
            throw new AssertionError("An AbstractAuthenticationException should have been thrown");
        } catch (final Throwable e) {
            assertNull(credentials.getNextToken());
            assertNull(credentials.getPrincipal());
        }
    }

    @Test
    void verifyUnsuccessfulAuthentication() {
        val credentials = new SpnegoCredential(new byte[]{0, 1, 2});
        val queue = new ArrayBlockingQueue<List<Authentication>>(POOL_SIZE);
        queue.add(CollectionUtils.wrapList(new MockUnsuccessfulJcifsAuthentication(false)));
        val authenticationHandler = new JcifsSpnegoAuthenticationHandler(getProperties(true, true), null, queue);

        authenticate(credentials, authenticationHandler);
    }

    @Test
    void verifySupports() {
        val queue = new ArrayBlockingQueue<List<Authentication>>(POOL_SIZE);
        queue.add(CollectionUtils.wrapList(new MockJcifsAuthentication()));
        val authenticationHandler = new JcifsSpnegoAuthenticationHandler(getProperties(true, true), null, queue);

        assertFalse(authenticationHandler.supports((Credential) null));
        assertTrue(authenticationHandler.supports(new SpnegoCredential(new byte[]{0, 1, 2})));
        assertFalse(authenticationHandler.supports(new UsernamePasswordCredential()));
    }

    @Test
    void verifyGetSimpleCredentials() throws Throwable {
        val myNtlmUser = "DOMAIN\\Username";
        val myNtlmUserWithNoDomain = USERNAME;
        val myKerberosUser = "Username@DOMAIN.COM";

        val queue = new ArrayBlockingQueue<List<Authentication>>(POOL_SIZE);
        queue.add(CollectionUtils.wrapList(new MockJcifsAuthentication()));

        val factory = PrincipalFactoryUtils.newPrincipalFactory();
        val authenticationHandler = new JcifsSpnegoAuthenticationHandler(getProperties(true, true), null, queue);

        assertEquals(factory.createPrincipal(myNtlmUser), authenticationHandler.getPrincipal(myNtlmUser, true));
        assertEquals(factory.createPrincipal(myNtlmUserWithNoDomain), authenticationHandler.getPrincipal(myNtlmUserWithNoDomain, false));
        assertEquals(factory.createPrincipal(myKerberosUser), authenticationHandler.getPrincipal(myKerberosUser, false));

        val handlerNoDomain = new JcifsSpnegoAuthenticationHandler(getProperties(false, true),
            PrincipalFactoryUtils.newPrincipalFactory(), queue);
        assertEquals(factory.createPrincipal(USERNAME), handlerNoDomain.getPrincipal(myNtlmUser, true));
        assertEquals(factory.createPrincipal(USERNAME), handlerNoDomain.getPrincipal(myNtlmUserWithNoDomain, true));
        assertEquals(factory.createPrincipal(USERNAME), handlerNoDomain.getPrincipal(myKerberosUser, false));
    }

    private static SpnegoProperties getProperties(final boolean principalWithDomainName, final boolean ntlmAllowed) {
        val prop = new SpnegoProperties();
        prop.setName(StringUtils.EMPTY);
        prop.setOrder(0);
        prop.setPrincipalWithDomainName(principalWithDomainName);
        prop.setNtlmAllowed(ntlmAllowed);
        return prop;
    }
}
