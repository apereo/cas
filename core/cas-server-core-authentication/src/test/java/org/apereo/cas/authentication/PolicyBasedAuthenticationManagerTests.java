package org.apereo.cas.authentication;

import org.apereo.cas.authentication.handler.DefaultAuthenticationHandlerResolver;
import org.apereo.cas.authentication.handler.RegisteredServiceAuthenticationHandlerResolver;
import org.apereo.cas.authentication.policy.AllCredentialsValidatedAuthenticationPolicy;
import org.apereo.cas.authentication.policy.AtLeastOneCredentialValidatedAuthenticationPolicy;
import org.apereo.cas.authentication.policy.RequiredHandlerAuthenticationPolicy;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;

import javax.security.auth.login.FailedLoginException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
@Tag("Simple")
public class PolicyBasedAuthenticationManagerTests {
    private static final String HANDLER_A = "HandlerA";

    private static final String HANDLER_B = "HandlerB";

    private final AuthenticationTransaction transaction = DefaultAuthenticationTransaction.of(CoreAuthenticationTestUtils.getService(),
        mock(Credential.class, withSettings().serializable()),
        mock(Credential.class, withSettings().serializable()));

    /**
     * Creates a new mock authentication handler that either successfully validates all credentials or fails to
     * validate all credentials.
     *
     * @param success True to authenticate all credentials, false to fail all credentials.
     * @return New mock authentication handler instance.
     */
    private static AuthenticationHandler newMockHandler(final boolean success) {
        return newMockHandler(success, false);
    }

    /**
     * Creates a new mock authentication handler that either successfully validates all credentials or fails to
     * validate all credentials.
     *
     * @param success True to authenticate all credentials, false to fail all credentials.
     * @param error   True if the handle has an error, false if not.
     * @return New mock authentication handler instance.
     */
    private static AuthenticationHandler newMockHandler(final boolean success, final boolean error) {
        val name = "MockAuthenticationHandler" + UUID.randomUUID().toString();
        return newMockHandler(name, success, error);
    }

    /**
     * Creates a new named mock authentication handler that either successfully validates all credentials or fails to
     * validate all credentials.
     *
     * @param name    Authentication handler name.
     * @param success True to authenticate all credentials, false to fail all credentials.
     * @return New mock authentication handler instance.
     */
    private static AuthenticationHandler newMockHandler(final String name, final boolean success) {
        return newMockHandler(name, success, false);
    }

    /**
     * Creates a new named mock authentication handler that either successfully validates all credentials or fails to
     * validate all credentials.
     *
     * @param name    Authentication handler name.
     * @param success True to authenticate all credentials, false to fail all credentials.
     * @param error   True if the handle has an error, false if not.
     * @return New mock authentication handler instance.
     */
    @SneakyThrows
    private static AuthenticationHandler newMockHandler(final String name, final boolean success, final boolean error) {
        val mock = mock(AuthenticationHandler.class);
        when(mock.getName()).thenReturn(name);
        when(mock.supports(any(Credential.class))).thenReturn(true);
        if (success) {
            val p = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("nobody");

            val result = new DefaultAuthenticationHandlerExecutionResult(mock, mock(CredentialMetaData.class), p);
            when(mock.authenticate(any(Credential.class))).thenReturn(result);
        } else if (!error) {
            when(mock.authenticate(any(Credential.class))).thenThrow(new FailedLoginException());
        } else {
            when(mock.authenticate(any(Credential.class))).thenThrow(new PreventedException("failure"));
        }
        return mock;
    }

    protected static ServicesManager mockServicesManager() {
        val svc = mock(ServicesManager.class);
        val reg = CoreAuthenticationTestUtils.getRegisteredService();
        when(svc.findServiceBy(any(Service.class))).thenReturn(reg);
        when(svc.getAllServices()).thenReturn(List.of(reg));
        return svc;
    }

    private static AuthenticationEventExecutionPlan getAuthenticationExecutionPlan(final Map<AuthenticationHandler, PrincipalResolver> map) {
        val plan = new DefaultAuthenticationEventExecutionPlan();
        plan.registerAuthenticationHandlerWithPrincipalResolver(map);
        plan.registerAuthenticationHandlerResolver(new RegisteredServiceAuthenticationHandlerResolver(mockServicesManager(),
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy())));
        plan.registerAuthenticationHandlerResolver(new DefaultAuthenticationHandlerResolver());
        return plan;
    }

    @Test
    public void verifyAuthenticateAnySuccess() {
        val map = new HashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(true), null);
        map.put(newMockHandler(false), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new AtLeastOneCredentialValidatedAuthenticationPolicy());
        val manager = new PolicyBasedAuthenticationManager(authenticationExecutionPlan,
            false, mock(ConfigurableApplicationContext.class));

        val auth = manager.authenticate(transaction);
        assertEquals(1, auth.getSuccesses().size());
        assertEquals(2, auth.getCredentials().size());
    }

    @Test
    public void verifyAuthenticateAnyButTryAllSuccess() {
        val map = new HashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(true), null);
        map.put(newMockHandler(false), null);
        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new AtLeastOneCredentialValidatedAuthenticationPolicy(true));
        val manager = new PolicyBasedAuthenticationManager(authenticationExecutionPlan,
            false, mock(ConfigurableApplicationContext.class));

        val auth = manager.authenticate(transaction);
        assertEquals(1, auth.getSuccesses().size());
        assertEquals(1, auth.getFailures().size());
        assertEquals(2, map.size());
    }

    @Test
    public void verifyAuthenticateAnyFailure() {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(false), null);
        map.put(newMockHandler(false), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new AtLeastOneCredentialValidatedAuthenticationPolicy());
        val manager = new PolicyBasedAuthenticationManager(authenticationExecutionPlan,
            false, mock(ConfigurableApplicationContext.class));

        assertThrows(AuthenticationException.class, () -> manager.authenticate(transaction));
    }

    @Test
    public void verifyAuthenticateAnyFailureWithError() {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(false, true), null);
        map.put(newMockHandler(false, true), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new AtLeastOneCredentialValidatedAuthenticationPolicy());
        val manager = new PolicyBasedAuthenticationManager(authenticationExecutionPlan,
            false, mock(ConfigurableApplicationContext.class));

        assertThrows(AuthenticationException.class, () -> manager.authenticate(transaction));
    }

    @Test
    public void verifyAuthenticateAllSuccess() {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(true), null);
        map.put(newMockHandler(true), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new AllCredentialsValidatedAuthenticationPolicy());
        val manager = new PolicyBasedAuthenticationManager(authenticationExecutionPlan,
            false, mock(ConfigurableApplicationContext.class));

        val auth = manager.authenticate(transaction);
        assertEquals(2, auth.getSuccesses().size());
        assertEquals(0, auth.getFailures().size());
        assertEquals(2, auth.getCredentials().size());
    }

    @Test
    public void verifyAuthenticateAllFailure() {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(false), null);
        map.put(newMockHandler(false), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new AllCredentialsValidatedAuthenticationPolicy());
        val manager = new PolicyBasedAuthenticationManager(authenticationExecutionPlan,
            false, mock(ConfigurableApplicationContext.class));

        assertThrows(AuthenticationException.class, () -> manager.authenticate(transaction));
    }

    @Test
    public void verifyAuthenticateRequiredHandlerSuccess() {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(HANDLER_A, true), null);
        map.put(newMockHandler(HANDLER_B, false), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new RequiredHandlerAuthenticationPolicy(HANDLER_A));
        val manager = new PolicyBasedAuthenticationManager(authenticationExecutionPlan,
            false, mock(ConfigurableApplicationContext.class));


        val auth = manager.authenticate(transaction);
        assertEquals(1, auth.getSuccesses().size());
        assertEquals(2, auth.getCredentials().size());
    }

    @Test
    public void verifyAuthenticateRequiredHandlerFailure() {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(HANDLER_A, true), null);
        map.put(newMockHandler(HANDLER_B, false), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new RequiredHandlerAuthenticationPolicy(HANDLER_B));
        val manager = new PolicyBasedAuthenticationManager(authenticationExecutionPlan,
            false, mock(ConfigurableApplicationContext.class));

        assertThrows(AuthenticationException.class, () -> manager.authenticate(transaction));
    }

    @Test
    public void verifyAuthenticateRequiredHandlerTryAllSuccess() {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(HANDLER_A, true), null);
        map.put(newMockHandler(HANDLER_B, false), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new RequiredHandlerAuthenticationPolicy(Set.of(HANDLER_A), true));
        val manager = new PolicyBasedAuthenticationManager(authenticationExecutionPlan, false,
            mock(ConfigurableApplicationContext.class));

        val auth = manager.authenticate(transaction);
        assertEquals(1, auth.getSuccesses().size());
        assertEquals(1, auth.getFailures().size());
        assertEquals(2, auth.getCredentials().size());
    }
}
