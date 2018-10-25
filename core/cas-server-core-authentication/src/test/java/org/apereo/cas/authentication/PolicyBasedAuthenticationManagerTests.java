package org.apereo.cas.authentication;

import org.apereo.cas.authentication.handler.DefaultAuthenticationHandlerResolver;
import org.apereo.cas.authentication.handler.RegisteredServiceAuthenticationHandlerResolver;
import org.apereo.cas.authentication.policy.AllCredentialsValidatedAuthenticationPolicy;
import org.apereo.cas.authentication.policy.AtLeastOneCredentialValidatedAuthenticationPolicy;
import org.apereo.cas.authentication.policy.RequiredHandlerAuthenticationPolicy;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.annotation.DirtiesContext;

import javax.security.auth.login.FailedLoginException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link PolicyBasedAuthenticationManager}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
public class PolicyBasedAuthenticationManagerTests {
    private static final String HANDLER_A = "HandlerA";
    private static final String HANDLER_B = "HandlerB";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final AuthenticationTransaction transaction = DefaultAuthenticationTransaction.of(CoreAuthenticationTestUtils.getService(),
        mock(Credential.class), mock(Credential.class));

    /**
     * Creates a new mock authentication handler that either successfully validates all credentials or fails to
     * validate all credentials.
     *
     * @param success True to authenticate all credentials, false to fail all credentials.
     * @return New mock authentication handler instance.
     * @throws Exception On errors.
     */
    private static AuthenticationHandler newMockHandler(final boolean success) throws Exception {
        val name = "MockAuthenticationHandler" + UUID.randomUUID().toString();
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
        val mock = mock(AuthenticationHandler.class);
        when(mock.getName()).thenReturn(name);
        when(mock.supports(any(Credential.class))).thenReturn(true);
        if (success) {
            val p = new DefaultPrincipalFactory().createPrincipal("nobody");

            val result = new DefaultAuthenticationHandlerExecutionResult(mock, mock(CredentialMetaData.class), p);
            when(mock.authenticate(any(Credential.class))).thenReturn(result);
        } else {
            when(mock.authenticate(any(Credential.class))).thenThrow(new FailedLoginException());
        }
        return mock;
    }

    @Test
    public void verifyAuthenticateAnySuccess() throws Exception {
        val map = new HashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(true), null);
        map.put(newMockHandler(false), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new AtLeastOneCredentialValidatedAuthenticationPolicy());
        val manager = new PolicyBasedAuthenticationManager(authenticationExecutionPlan,
            false, mock(ApplicationEventPublisher.class));

        val auth = manager.authenticate(transaction);
        assertEquals(1, auth.getSuccesses().size());
        assertEquals(2, auth.getCredentials().size());
    }

    @Test
    public void verifyAuthenticateAnyButTryAllSuccess() throws Exception {
        val map = new HashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(true), null);
        map.put(newMockHandler(false), null);
        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new AtLeastOneCredentialValidatedAuthenticationPolicy(true));
        val manager = new PolicyBasedAuthenticationManager(authenticationExecutionPlan,
            false, mock(ApplicationEventPublisher.class));

        val auth = manager.authenticate(transaction);
        assertEquals(1, auth.getSuccesses().size());
        assertEquals(1, auth.getFailures().size());
        assertEquals(2, auth.getCredentials().size());
    }

    protected ServicesManager mockServicesManager() {
        val svc = mock(ServicesManager.class);
        val reg = CoreAuthenticationTestUtils.getRegisteredService();
        when(svc.findServiceBy(any(Service.class))).thenReturn(reg);
        when(svc.getAllServices()).thenReturn((Collection) Collections.singletonList(reg));
        return svc;
    }

    @Test
    public void verifyAuthenticateAnyFailure() throws Exception {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(false), null);
        map.put(newMockHandler(false), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new AtLeastOneCredentialValidatedAuthenticationPolicy());
        val manager = new PolicyBasedAuthenticationManager(authenticationExecutionPlan,
            false, mock(ApplicationEventPublisher.class));

        this.thrown.expect(AuthenticationException.class);
        manager.authenticate(transaction);

        throw new AssertionError("Should have thrown authentication exception");
    }

    @Test
    public void verifyAuthenticateAllSuccess() throws Exception {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(true), null);
        map.put(newMockHandler(true), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new AllCredentialsValidatedAuthenticationPolicy());
        val manager = new PolicyBasedAuthenticationManager(authenticationExecutionPlan,
            false, mock(ApplicationEventPublisher.class));

        val auth = manager.authenticate(transaction);
        assertEquals(2, auth.getSuccesses().size());
        assertEquals(0, auth.getFailures().size());
        assertEquals(2, auth.getCredentials().size());
    }

    @Test
    public void verifyAuthenticateAllFailure() throws Exception {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(false), null);
        map.put(newMockHandler(false), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new AllCredentialsValidatedAuthenticationPolicy());
        val manager = new PolicyBasedAuthenticationManager(authenticationExecutionPlan,
            false, mock(ApplicationEventPublisher.class));

        this.thrown.expect(AuthenticationException.class);
        manager.authenticate(transaction);

        throw new AssertionError("Should have thrown authentication exception");
    }

    @Test
    public void verifyAuthenticateRequiredHandlerSuccess() throws Exception {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(HANDLER_A, true), null);
        map.put(newMockHandler(HANDLER_B, false), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new RequiredHandlerAuthenticationPolicy(HANDLER_A));
        val manager = new PolicyBasedAuthenticationManager(authenticationExecutionPlan,
            false, mock(ApplicationEventPublisher.class));


        val auth = manager.authenticate(transaction);
        assertEquals(1, auth.getSuccesses().size());
        assertEquals(2, auth.getCredentials().size());
    }

    @Test
    public void verifyAuthenticateRequiredHandlerFailure() throws Exception {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(HANDLER_A, true), null);
        map.put(newMockHandler(HANDLER_B, false), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new RequiredHandlerAuthenticationPolicy(HANDLER_B));
        val manager = new PolicyBasedAuthenticationManager(authenticationExecutionPlan,
            false, mock(ApplicationEventPublisher.class));

        this.thrown.expect(AuthenticationException.class);
        manager.authenticate(transaction);
        throw new AssertionError("Should have thrown AuthenticationException");
    }

    @Test
    public void verifyAuthenticateRequiredHandlerTryAllSuccess() throws Exception {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(HANDLER_A, true), null);
        map.put(newMockHandler(HANDLER_B, false), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new RequiredHandlerAuthenticationPolicy(HANDLER_A, true));
        val manager = new PolicyBasedAuthenticationManager(authenticationExecutionPlan, false,
            mock(ApplicationEventPublisher.class));

        val auth = manager.authenticate(transaction);
        assertEquals(1, auth.getSuccesses().size());
        assertEquals(1, auth.getFailures().size());
        assertEquals(2, auth.getCredentials().size());
    }

    private AuthenticationEventExecutionPlan getAuthenticationExecutionPlan(final Map<AuthenticationHandler, PrincipalResolver> map) {
        val plan = new DefaultAuthenticationEventExecutionPlan();
        plan.registerAuthenticationHandlerWithPrincipalResolver(map);
        plan.registerAuthenticationHandlerResolver(new RegisteredServiceAuthenticationHandlerResolver(mockServicesManager()));
        plan.registerAuthenticationHandlerResolver(new DefaultAuthenticationHandlerResolver());
        return plan;
    }
}
