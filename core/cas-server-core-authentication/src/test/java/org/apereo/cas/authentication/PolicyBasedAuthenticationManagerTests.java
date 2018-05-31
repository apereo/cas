package org.apereo.cas.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.policy.AllAuthenticationPolicy;
import org.apereo.cas.authentication.policy.AnyAuthenticationPolicy;
import org.apereo.cas.authentication.policy.RequiredHandlerAuthenticationPolicy;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.annotation.DirtiesContext;

import javax.security.auth.login.FailedLoginException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link PolicyBasedAuthenticationManager}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
@Slf4j
public class PolicyBasedAuthenticationManagerTests {

    private static final String HANDLER_A = "HandlerA";
    private static final String HANDLER_B = "HandlerB";
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final AuthenticationTransaction transaction = DefaultAuthenticationTransaction.of(CoreAuthenticationTestUtils.getService(),
        mock(Credential.class), mock(Credential.class));

    @Test
    public void verifyAuthenticateAnySuccess() throws Exception {
        final Map<AuthenticationHandler, PrincipalResolver> map = new HashMap<>();
        map.put(newMockHandler(true), null);
        map.put(newMockHandler(false), null);

        final var authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new AnyAuthenticationPolicy());
        final var manager = new PolicyBasedAuthenticationManager(authenticationExecutionPlan,
            false, mock(ApplicationEventPublisher.class));

        final var auth = manager.authenticate(transaction);
        assertEquals(1, auth.getSuccesses().size());
        assertEquals(2, auth.getCredentials().size());
    }

    @Test
    public void verifyAuthenticateAnyButTryAllSuccess() throws Exception {
        final Map<AuthenticationHandler, PrincipalResolver> map = new HashMap<>();
        map.put(newMockHandler(true), null);
        map.put(newMockHandler(false), null);
        final var authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new AnyAuthenticationPolicy(true));
        final var manager = new PolicyBasedAuthenticationManager(authenticationExecutionPlan,
            false, mock(ApplicationEventPublisher.class));

        final var auth = manager.authenticate(transaction);
        assertEquals(1, auth.getSuccesses().size());
        assertEquals(1, auth.getFailures().size());
        assertEquals(2, auth.getCredentials().size());
    }

    protected ServicesManager mockServicesManager() {
        final var svc = mock(ServicesManager.class);
        final var reg = CoreAuthenticationTestUtils.getRegisteredService();
        when(svc.findServiceBy(any(Service.class))).thenReturn(reg);
        when(svc.getAllServices()).thenReturn(Collections.singletonList(reg));
        return svc;
    }

    @Test
    public void verifyAuthenticateAnyFailure() throws Exception {
        final Map<AuthenticationHandler, PrincipalResolver> map = new LinkedHashMap<>();
        map.put(newMockHandler(false), null);
        map.put(newMockHandler(false), null);

        final var authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new AnyAuthenticationPolicy());
        final var manager = new PolicyBasedAuthenticationManager(authenticationExecutionPlan,
            false, mock(ApplicationEventPublisher.class));

        this.thrown.expect(AuthenticationException.class);
        manager.authenticate(transaction);

        throw new AssertionError("Should have thrown authentication exception");
    }

    @Test
    public void verifyAuthenticateAllSuccess() throws Exception {
        final Map<AuthenticationHandler, PrincipalResolver> map = new LinkedHashMap<>();
        map.put(newMockHandler(true), null);
        map.put(newMockHandler(true), null);

        final var authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new AllAuthenticationPolicy());
        final var manager = new PolicyBasedAuthenticationManager(authenticationExecutionPlan,
            false, mock(ApplicationEventPublisher.class));

        final var auth = manager.authenticate(transaction);
        assertEquals(2, auth.getSuccesses().size());
        assertEquals(0, auth.getFailures().size());
        assertEquals(2, auth.getCredentials().size());
    }

    @Test
    public void verifyAuthenticateAllFailure() throws Exception {
        final Map<AuthenticationHandler, PrincipalResolver> map = new LinkedHashMap<>();
        map.put(newMockHandler(false), null);
        map.put(newMockHandler(false), null);

        final var authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new AllAuthenticationPolicy());
        final var manager = new PolicyBasedAuthenticationManager(authenticationExecutionPlan,
            false, mock(ApplicationEventPublisher.class));

        this.thrown.expect(AuthenticationException.class);
        manager.authenticate(transaction);

        throw new AssertionError("Should have thrown authentication exception");
    }

    @Test
    public void verifyAuthenticateRequiredHandlerSuccess() throws Exception {
        final Map<AuthenticationHandler, PrincipalResolver> map = new LinkedHashMap<>();
        map.put(newMockHandler(HANDLER_A, true), null);
        map.put(newMockHandler(HANDLER_B, false), null);

        final var authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new RequiredHandlerAuthenticationPolicy(HANDLER_A));
        final var manager = new PolicyBasedAuthenticationManager(authenticationExecutionPlan,
            false, mock(ApplicationEventPublisher.class));


        final var auth = manager.authenticate(transaction);
        assertEquals(1, auth.getSuccesses().size());
        assertEquals(2, auth.getCredentials().size());
    }

    @Test
    public void verifyAuthenticateRequiredHandlerFailure() throws Exception {
        final Map<AuthenticationHandler, PrincipalResolver> map = new LinkedHashMap<>();
        map.put(newMockHandler(HANDLER_A, true), null);
        map.put(newMockHandler(HANDLER_B, false), null);

        final var authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new RequiredHandlerAuthenticationPolicy(HANDLER_B));
        final var manager = new PolicyBasedAuthenticationManager(authenticationExecutionPlan,
            false, mock(ApplicationEventPublisher.class));

        this.thrown.expect(AuthenticationException.class);
        manager.authenticate(transaction);
        throw new AssertionError("Should have thrown AuthenticationException");
    }

    @Test
    public void verifyAuthenticateRequiredHandlerTryAllSuccess() throws Exception {
        final Map<AuthenticationHandler, PrincipalResolver> map = new LinkedHashMap<>();
        map.put(newMockHandler(HANDLER_A, true), null);
        map.put(newMockHandler(HANDLER_B, false), null);

        final var authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new RequiredHandlerAuthenticationPolicy(HANDLER_A, true));
        final var manager = new PolicyBasedAuthenticationManager(authenticationExecutionPlan, false,
            mock(ApplicationEventPublisher.class));

        final var auth = manager.authenticate(transaction);
        assertEquals(1, auth.getSuccesses().size());
        assertEquals(1, auth.getFailures().size());
        assertEquals(2, auth.getCredentials().size());
    }

    /**
     * Creates a new mock authentication handler that either successfully validates all credentials or fails to
     * validate all credentials.
     *
     * @param success True to authenticate all credentials, false to fail all credentials.
     * @return New mock authentication handler instance.
     * @throws Exception On errors.
     */
    private static AuthenticationHandler newMockHandler(final boolean success) throws Exception {
        final var name = "MockAuthenticationHandler" + UUID.randomUUID().toString();
        return newMockHandler(name, success);
    }

    /**
     * Creates a new named mock authentication handler that either successfully validates all credentials or fails to
     * validate all credentials.
     *
     * @param name    Authentication handler name.
     * @param success True to authenticate all credentials, false to fail all credentials.
     * @return New mock authentication handler instance.
     * @throws Exception On errors.
     */
    private static AuthenticationHandler newMockHandler(final String name, final boolean success) throws Exception {
        final var mock = mock(AuthenticationHandler.class);
        when(mock.getName()).thenReturn(name);
        when(mock.supports(any(Credential.class))).thenReturn(true);
        if (success) {
            final var p = new DefaultPrincipalFactory().createPrincipal("nobody");

            final AuthenticationHandlerExecutionResult result = new DefaultAuthenticationHandlerExecutionResult(mock, mock(CredentialMetaData.class), p);
            when(mock.authenticate(any(Credential.class))).thenReturn(result);
        } else {
            when(mock.authenticate(any(Credential.class))).thenThrow(new FailedLoginException());
        }
        return mock;
    }

    private AuthenticationEventExecutionPlan getAuthenticationExecutionPlan(final Map<AuthenticationHandler, PrincipalResolver> map) {
        final var plan = new DefaultAuthenticationEventExecutionPlan();
        plan.registerAuthenticationHandlerWithPrincipalResolver(map);
        plan.registerAuthenticationHandlerResolver(new RegisteredServiceAuthenticationHandlerResolver(mockServicesManager()));
        plan.registerAuthenticationHandlerResolver(new DefaultAuthenticationHandlerResolver());
        return plan;
    }
}
