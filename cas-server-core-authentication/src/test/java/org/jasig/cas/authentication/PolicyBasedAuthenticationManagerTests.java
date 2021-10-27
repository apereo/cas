package org.jasig.cas.authentication;

import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.principal.Principal;
import org.junit.Test;

import javax.security.auth.login.FailedLoginException;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


/**
 * Unit test for {@link PolicyBasedAuthenticationManager}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class PolicyBasedAuthenticationManagerTests {

    private final AuthenticationTransaction transaction = AuthenticationTransaction.wrap(mock(Credential.class), mock(Credential.class));

    @Test
    public void verifyAuthenticateAnySuccess() throws Exception {
        final PolicyBasedAuthenticationManager manager = new PolicyBasedAuthenticationManager(
                newMockHandler(true),
                newMockHandler(false));
        final Authentication auth = manager.authenticate(transaction);
        assertEquals(1, auth.getSuccesses().size());
        assertEquals(0, auth.getFailures().size());
        assertEquals(2, auth.getCredentials().size());
    }

    @Test
    public void verifyAuthenticateAnyButTryAllSuccess() throws Exception {
        final PolicyBasedAuthenticationManager manager = new PolicyBasedAuthenticationManager(
                newMockHandler(true),
                newMockHandler(false));
        final AnyAuthenticationPolicy any = new AnyAuthenticationPolicy();
        any.setTryAll(true);
        manager.setAuthenticationPolicy(any);
        final Authentication auth = manager.authenticate(transaction);
        assertEquals(1, auth.getSuccesses().size());
        assertEquals(1, auth.getFailures().size());
        assertEquals(2, auth.getCredentials().size());
    }

    @Test(expected = AuthenticationException.class)
    public void verifyAuthenticateAnyFailure() throws Exception {
        final PolicyBasedAuthenticationManager manager = new PolicyBasedAuthenticationManager(
                newMockHandler(false),
                newMockHandler(false));
        manager.authenticate(transaction);
        fail("Should have thrown authentication exception");
    }

    @Test
    public void verifyAuthenticateAllSuccess() throws Exception {
        final PolicyBasedAuthenticationManager manager = new PolicyBasedAuthenticationManager(
                newMockHandler(true),
                newMockHandler(true));
        manager.setAuthenticationPolicy(new AllAuthenticationPolicy());
        final Authentication auth = manager.authenticate(transaction);
        assertEquals(2, auth.getSuccesses().size());
        assertEquals(0, auth.getFailures().size());
        assertEquals(2, auth.getCredentials().size());
    }

    @Test(expected = AuthenticationException.class)
    public void verifyAuthenticateAllFailure() throws Exception {
        final PolicyBasedAuthenticationManager manager = new PolicyBasedAuthenticationManager(
                newMockHandler(false),
                newMockHandler(false));
        manager.setAuthenticationPolicy(new AllAuthenticationPolicy());
        manager.authenticate(transaction);
        fail("Should have thrown authentication exception");
    }

    @Test
    public void verifyAuthenticateRequiredHandlerSuccess() throws Exception {
        final PolicyBasedAuthenticationManager manager = new PolicyBasedAuthenticationManager(
                newMockHandler("HandlerA", true),
                newMockHandler("HandlerB", false));
        manager.setAuthenticationPolicy(new RequiredHandlerAuthenticationPolicy("HandlerA"));
        final Authentication auth = manager.authenticate(transaction);
        assertEquals(1, auth.getSuccesses().size());
        assertEquals(0, auth.getFailures().size());
        assertEquals(2, auth.getCredentials().size());
    }

    @Test(expected = AuthenticationException.class)
    public void verifyAuthenticateRequiredHandlerFailure() throws Exception {
        final PolicyBasedAuthenticationManager manager = new PolicyBasedAuthenticationManager(
                newMockHandler("HandlerA", true),
                newMockHandler("HandlerB", false));
        manager.setAuthenticationPolicy(new RequiredHandlerAuthenticationPolicy("HandlerB"));
        manager.authenticate(transaction);
        fail("Should have thrown AbstractAuthenticationException");
    }

    @Test
    public void verifyAuthenticateRequiredHandlerTryAllSuccess() throws Exception {
        final PolicyBasedAuthenticationManager manager = new PolicyBasedAuthenticationManager(
                newMockHandler("HandlerA", true),
                newMockHandler("HandlerB", false));
        final RequiredHandlerAuthenticationPolicy policy = new RequiredHandlerAuthenticationPolicy("HandlerA");
        policy.setTryAll(true);
        manager.setAuthenticationPolicy(policy);
        final Authentication auth = manager.authenticate(transaction);
        assertEquals(1, auth.getSuccesses().size());
        assertEquals(1, auth.getFailures().size());
        assertEquals(2, auth.getCredentials().size());
    }

    /**
     * Creates a new mock authentication handler that either successfully validates all credentials or fails to
     * validate all credentials.
     *
     * @param success True to authenticate all credentials, false to fail all credentials.
     *
     * @return New mock authentication handler instance.
     *
     * @throws Exception On errors.
     */
    private static AuthenticationHandler newMockHandler(final boolean success) throws Exception {
        return newMockHandler("MockAuthenticationHandler" + System.nanoTime(), success);
    }

    /**
     * Creates a new named mock authentication handler that either successfully validates all credentials or fails to
     * validate all credentials.
     *
     * @param name Authentication handler name.
     * @param success True to authenticate all credentials, false to fail all credentials.
     *
     * @return New mock authentication handler instance.
     *
     * @throws Exception On errors.
     */
    private static AuthenticationHandler newMockHandler(final String name, final boolean success) throws Exception {
        final AuthenticationHandler mock = mock(AuthenticationHandler.class);
        when(mock.getName()).thenReturn(name);
        when(mock.supports(any(Credential.class))).thenReturn(true);
        if (success) {
            final Principal p = new DefaultPrincipalFactory().createPrincipal("nobody");

            final HandlerResult result = new DefaultHandlerResult(
                    mock,
                    mock(CredentialMetaData.class),
                    p);
            when(mock.authenticate(any(Credential.class))).thenReturn(result);
        } else {
            when(mock.authenticate(any(Credential.class))).thenThrow(new FailedLoginException());
        }
        return mock;
    }

}
