package org.apereo.cas.authentication;

import org.apereo.cas.authentication.exceptions.UnresolvedPrincipalException;
import org.apereo.cas.authentication.handler.DefaultAuthenticationHandlerResolver;
import org.apereo.cas.authentication.handler.RegisteredServiceAuthenticationHandlerResolver;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.policy.AllCredentialsValidatedAuthenticationPolicy;
import org.apereo.cas.authentication.policy.AtLeastOneCredentialValidatedAuthenticationPolicy;
import org.apereo.cas.authentication.policy.RequiredAuthenticationHandlerAuthenticationPolicy;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link DefaultAuthenticationManager}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Tag("Authentication")
@Slf4j
public class DefaultAuthenticationManagerTests {
    private static final String HANDLER_A = "HandlerA";

    private static final String HANDLER_B = "HandlerB";

    private final AuthenticationTransaction transaction = new DefaultAuthenticationTransactionFactory()
        .newTransaction(CoreAuthenticationTestUtils.getService(),
            mock(Credential.class, withSettings().serializable()),
            mock(Credential.class, withSettings().serializable()));

    private ConfigurableApplicationContext applicationContext;

    protected static ServicesManager mockServicesManager() {
        val svc = mock(ServicesManager.class);
        val reg = CoreAuthenticationTestUtils.getRegisteredService();
        when(svc.findServiceBy(any(Service.class))).thenReturn(reg);
        when(svc.getAllServices()).thenReturn(List.of(reg));
        return svc;
    }

    /**
     * Creates a new mock authentication handler that either
     * successfully validates all credentials or fails to
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
        when(mock.getState()).thenCallRealMethod();
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

    private static AuthenticationEventExecutionPlan getAuthenticationExecutionPlan(final Map<AuthenticationHandler, PrincipalResolver> map) {
        val plan = new DefaultAuthenticationEventExecutionPlan();
        plan.registerAuthenticationHandlerWithPrincipalResolver(map);
        plan.registerAuthenticationHandlerResolver(new RegisteredServiceAuthenticationHandlerResolver(mockServicesManager(),
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy())));
        plan.registerAuthenticationHandlerResolver(new DefaultAuthenticationHandlerResolver());
        plan.registerAuthenticationPostProcessor((builder, transaction) -> LOGGER.trace("Running authentication post processor"));
        return plan;
    }

    @BeforeEach
    public void setup() {
        applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            CoreAuthenticationTestUtils.getAuthenticationSystemSupport(), AuthenticationSystemSupport.BEAN_NAME);
    }

    @Test
    public void verifyAuthenticateFailsPreProcessor() {
        val map = new HashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(true), null);
        map.put(newMockHandler(false), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPreProcessor(transaction -> false);

        val manager = new DefaultAuthenticationManager(authenticationExecutionPlan,
            false, applicationContext);
        assertThrows(AuthenticationException.class, () -> manager.authenticate(transaction));
    }

    @Test
    public void verifyNoHandlers() {
        val map = new HashMap<AuthenticationHandler, PrincipalResolver>();
        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        val manager = new DefaultAuthenticationManager(authenticationExecutionPlan,
            false, applicationContext);
        assertThrows(AuthenticationException.class, () -> manager.authenticate(transaction));
    }

    @Test
    public void verifyTransactionWithAuthnHistoryAndAuthnPolicy() {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(true), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        val policy = new RequiredAuthenticationHandlerAuthenticationPolicy(
            SimpleTestUsernamePasswordAuthenticationHandler.class.getSimpleName());
        authenticationExecutionPlan.registerAuthenticationPolicy(policy);
        val manager = new DefaultAuthenticationManager(authenticationExecutionPlan,
            false, applicationContext);

        val testTransaction = new DefaultAuthenticationTransactionFactory()
            .newTransaction(CoreAuthenticationTestUtils.getService(), mock(Credential.class, withSettings().serializable()));
        testTransaction.collect(List.of(CoreAuthenticationTestUtils.getAuthentication()));
        assertNotNull(manager.authenticate(testTransaction));
    }

    @Test
    public void verifyBlockingAuthnPolicy() throws Exception {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(false, true), null);
        map.put(newMockHandler(true), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        val policy = mock(AuthenticationPolicy.class);
        when(policy.isSatisfiedBy(any(), any(), any(), any()))
            .thenReturn(AuthenticationPolicyExecutionResult.success());
        when(policy.shouldResumeOnFailure(any())).thenReturn(Boolean.FALSE);

        authenticationExecutionPlan.registerAuthenticationPolicy(policy);
        val manager = new DefaultAuthenticationManager(authenticationExecutionPlan,
            false, applicationContext);

        val testTransaction = new DefaultAuthenticationTransactionFactory().newTransaction(CoreAuthenticationTestUtils.getService(),
            mock(Credential.class, withSettings().serializable()));

        assertThrows(AuthenticationException.class, () -> manager.authenticate(testTransaction));
    }

    @Test
    public void verifyResolverFails() {
        val map = new HashMap<AuthenticationHandler, PrincipalResolver>();
        val resolver = mock(PrincipalResolver.class);
        when(resolver.supports(any())).thenReturn(Boolean.FALSE);

        map.put(newMockHandler(true), resolver);
        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        val manager = new DefaultAuthenticationManager(authenticationExecutionPlan,
            false, applicationContext);
        assertThrows(UnresolvedPrincipalException.class, () -> manager.authenticate(transaction));

        when(resolver.supports(any())).thenReturn(Boolean.TRUE);
        when(resolver.resolve(any(), any(), any())).thenThrow(new RuntimeException("Fails"));
        assertThrows(UnresolvedPrincipalException.class, () -> manager.authenticate(transaction));
    }

    @Test
    public void verifyResolverFailsAsFatal() {
        val map = new HashMap<AuthenticationHandler, PrincipalResolver>();
        val resolver = mock(PrincipalResolver.class);
        when(resolver.supports(any())).thenReturn(Boolean.FALSE);

        map.put(newMockHandler(true), resolver);
        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        val manager = new DefaultAuthenticationManager(authenticationExecutionPlan,
            true, applicationContext);
        assertThrows(UnresolvedPrincipalException.class, () -> manager.authenticate(transaction));
    }

    @Test
    public void verifyAuthWithNoCreds() {
        val map = new HashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(true), null);
        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        val manager = new DefaultAuthenticationManager(authenticationExecutionPlan,
            true, applicationContext);
        assertThrows(AuthenticationException.class, () -> manager.authenticate(new DefaultAuthenticationTransactionFactory().newTransaction()));
    }

    @Test
    public void verifyAuthenticateAnySuccess() {
        val map = new HashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(true), null);
        map.put(newMockHandler(false), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new AtLeastOneCredentialValidatedAuthenticationPolicy());
        val manager = new DefaultAuthenticationManager(authenticationExecutionPlan,
            false, applicationContext);

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
        val manager = new DefaultAuthenticationManager(authenticationExecutionPlan,
            false, applicationContext);

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
        val manager = new DefaultAuthenticationManager(authenticationExecutionPlan,
            false, applicationContext);

        assertThrows(AuthenticationException.class, () -> manager.authenticate(transaction));
    }

    @Test
    public void verifyAuthenticateAnyFailureWithError() {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(false, true), null);
        map.put(newMockHandler(false, true), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new AtLeastOneCredentialValidatedAuthenticationPolicy());
        val manager = new DefaultAuthenticationManager(authenticationExecutionPlan,
            false, applicationContext);

        assertThrows(AuthenticationException.class, () -> manager.authenticate(transaction));
    }

    @Test
    public void verifyAuthenticateAllSuccess() {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(true), null);
        map.put(newMockHandler(true), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new AllCredentialsValidatedAuthenticationPolicy());
        val manager = new DefaultAuthenticationManager(authenticationExecutionPlan,
            false, applicationContext);

        val auth = manager.authenticate(transaction);
        assertEquals(2, auth.getSuccesses().size());
        assertEquals(0, auth.getFailures().size());
        assertEquals(2, auth.getCredentials().size());
    }

    @Test
    public void verifyAuthenticatePolicyFailsGeneric() throws Exception {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(true), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        val policy = mock(AuthenticationPolicy.class);
        when(policy.isSatisfiedBy(any(), any(), any(), any()))
            .thenThrow(new GeneralSecurityException(new FailedLoginException()));
        authenticationExecutionPlan.registerAuthenticationPolicy(policy);
        val manager = new DefaultAuthenticationManager(authenticationExecutionPlan,
            false, applicationContext);

        assertThrows(AuthenticationException.class, () -> manager.authenticate(transaction));
    }

    @Test
    public void verifyAuthenticatePolicyFails() throws Exception {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(true), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        val policy = mock(AuthenticationPolicy.class);
        when(policy.isSatisfiedBy(any(), any(), any(), any()))
            .thenThrow(new IllegalArgumentException());
        authenticationExecutionPlan.registerAuthenticationPolicy(policy);
        val manager = new DefaultAuthenticationManager(authenticationExecutionPlan,
            false, applicationContext);

        assertThrows(AuthenticationException.class, () -> manager.authenticate(transaction));
    }

    @Test
    public void verifyAuthenticateAllFailure() {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(false), null);
        map.put(newMockHandler(false), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new AllCredentialsValidatedAuthenticationPolicy());
        val manager = new DefaultAuthenticationManager(authenticationExecutionPlan,
            false, applicationContext);

        assertThrows(AuthenticationException.class, () -> manager.authenticate(transaction));
    }

    @Test
    public void verifyAuthenticateRequiredHandlerSuccess() {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(HANDLER_A, true), null);
        map.put(newMockHandler(HANDLER_B, false), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new RequiredAuthenticationHandlerAuthenticationPolicy(HANDLER_A));
        val manager = new DefaultAuthenticationManager(authenticationExecutionPlan,
            false, applicationContext);


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
        authenticationExecutionPlan.registerAuthenticationPolicy(new RequiredAuthenticationHandlerAuthenticationPolicy(HANDLER_B));
        val manager = new DefaultAuthenticationManager(authenticationExecutionPlan,
            false, applicationContext);

        assertThrows(AuthenticationException.class, () -> manager.authenticate(transaction));
    }

    @Test
    public void verifyAuthenticateRequiredHandlerTryAllSuccess() {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(HANDLER_A, true), null);
        map.put(newMockHandler(HANDLER_B, false), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new RequiredAuthenticationHandlerAuthenticationPolicy(Set.of(HANDLER_A), true));
        val manager = new DefaultAuthenticationManager(authenticationExecutionPlan, false,
            applicationContext);

        val auth = manager.authenticate(transaction);
        assertEquals(1, auth.getSuccesses().size());
        assertEquals(1, auth.getFailures().size());
        assertEquals(2, auth.getCredentials().size());
    }
}
