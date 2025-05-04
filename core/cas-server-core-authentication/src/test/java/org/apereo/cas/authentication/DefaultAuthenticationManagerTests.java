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
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.spring.DirectObjectProvider;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.StaticApplicationContext;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    DefaultAuthenticationManagerTests.AuthenticationPlanTestConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreEnvironmentBootstrapAutoConfiguration.class,
    CasCoreMultitenancyAutoConfiguration.class
})
@ExtendWith(CasTestExtension.class)
class DefaultAuthenticationManagerTests {
    private static final String HANDLER_A = "HandlerA";

    private static final String HANDLER_B = "HandlerB";

    private final AuthenticationTransaction transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory()
        .newTransaction(CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser1"),
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser2"));

    @Autowired
    @Qualifier(TenantExtractor.BEAN_NAME)
    private TenantExtractor tenantExtractor;
    
    private ConfigurableApplicationContext applicationContext;

    protected static ServicesManager mockServicesManager() {
        val svc = mock(ServicesManager.class);
        val reg = CoreAuthenticationTestUtils.getRegisteredService();
        when(svc.findServiceBy(any(Service.class))).thenReturn(reg);
        when(svc.getAllServices()).thenReturn(List.of(reg));
        return svc;
    }

    private static AuthenticationHandler newMockHandler(final boolean success) throws Throwable {
        return newMockHandler(success, false);
    }

    private static AuthenticationHandler newMockHandler(final boolean success, final boolean error) throws Throwable {
        val name = "MockAuthenticationHandler%s".formatted(UUID.randomUUID());
        return newMockHandler(name, success, error);
    }

    private static AuthenticationHandler newMockHandler(final String name, final boolean success) throws Throwable {
        return newMockHandler(name, success, false);
    }

    private static AuthenticationHandler newMockHandler(final String name, final boolean success, final boolean error) throws Throwable {
        val mock = mock(AuthenticationHandler.class);
        when(mock.getName()).thenReturn(name);
        when(mock.supports(any(Credential.class))).thenReturn(true);
        when(mock.getState()).thenCallRealMethod();
        if (success) {
            val principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("nobody");
            val metadata = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("nobody");
            val result = new DefaultAuthenticationHandlerExecutionResult(mock, metadata, principal);
            when(mock.authenticate(any(Credential.class), any(Service.class))).thenReturn(result);
        } else if (!error) {
            when(mock.authenticate(any(Credential.class), any(Service.class))).thenThrow(new FailedLoginException());
        } else {
            when(mock.authenticate(any(Credential.class), any(Service.class))).thenThrow(new PreventedException("failure"));
        }
        return mock;
    }

    private AuthenticationEventExecutionPlan getAuthenticationExecutionPlan(
        final Map<AuthenticationHandler, PrincipalResolver> map) {
        val plan = new DefaultAuthenticationEventExecutionPlan(new DefaultAuthenticationHandlerResolver(), tenantExtractor);
        plan.registerAuthenticationHandlerWithPrincipalResolver(map);
        plan.registerAuthenticationHandlerResolver(new RegisteredServiceAuthenticationHandlerResolver(mockServicesManager(),
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy())));
        plan.registerAuthenticationHandlerResolver(new DefaultAuthenticationHandlerResolver());
        plan.registerAuthenticationPostProcessor(AuthenticationPostProcessor.none());
        return plan;
    }

    @BeforeEach
    void setup() throws Throwable {
        applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            CoreAuthenticationTestUtils.getAuthenticationSystemSupport(), AuthenticationSystemSupport.BEAN_NAME);
        val context = MockRequestContext.create(applicationContext);
        context.setRemoteAddr("185.86.151.11").setLocalAddr("185.86.151.11").setClientInfo();
    }

    @Test
    void verifyAuthenticateFailsPreProcessor() throws Throwable {
        val map = new HashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(true), null);
        map.put(newMockHandler(false), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPreProcessor(__ -> false);

        val manager = getAuthenticationManager(authenticationExecutionPlan);
        assertThrows(AuthenticationException.class, () -> manager.authenticate(transaction));
    }

    @Test
    void verifyNoHandlers() {
        val map = new HashMap<AuthenticationHandler, PrincipalResolver>();
        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        val manager = getAuthenticationManager(authenticationExecutionPlan);
        assertThrows(AuthenticationException.class, () -> manager.authenticate(transaction));
    }

    @Test
    void verifyHandlerDoesNotSupportCredential() throws Throwable {
        val map = new HashMap<AuthenticationHandler, PrincipalResolver>();
        val handler = newMockHandler(true);
        when(handler.supports(any(Credential.class))).thenReturn(Boolean.FALSE);
        map.put(handler, null);
        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        val manager = getAuthenticationManager(authenticationExecutionPlan);
        assertThrows(AuthenticationException.class, () -> manager.authenticate(transaction));
    }

    @Test
    void verifyTransactionWithAuthnHistoryAndAuthnPolicy() throws Throwable {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(true), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        val policy = new RequiredAuthenticationHandlerAuthenticationPolicy(
            SimpleTestUsernamePasswordAuthenticationHandler.class.getSimpleName());
        authenticationExecutionPlan.registerAuthenticationPolicy(policy);
        val manager = getAuthenticationManager(authenticationExecutionPlan);

        val testTransaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory()
            .newTransaction(CoreAuthenticationTestUtils.getService(), mock(Credential.class, withSettings().serializable()));
        testTransaction.collect(List.of(CoreAuthenticationTestUtils.getAuthentication()));
        assertNotNull(manager.authenticate(testTransaction));
    }

    @Test
    void verifyBlockingAuthnPolicy() throws Throwable {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(false, true), null);
        map.put(newMockHandler(true), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        val policy = mock(AuthenticationPolicy.class);
        when(policy.isSatisfiedBy(any(), anySet(), any(), anyMap()))
            .thenReturn(AuthenticationPolicyExecutionResult.success());
        when(policy.shouldResumeOnFailure(any())).thenReturn(Boolean.FALSE);


        authenticationExecutionPlan.registerAuthenticationPolicy(policy);
        val manager = getAuthenticationManager(authenticationExecutionPlan);

        val testTransaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory()
            .newTransaction(CoreAuthenticationTestUtils.getService(), mock(Credential.class, withSettings().serializable()));

        assertThrows(AuthenticationException.class, () -> manager.authenticate(testTransaction));
    }

    @Test
    void verifyResolverFails() throws Throwable {
        val map = new HashMap<AuthenticationHandler, PrincipalResolver>();
        val resolver = mock(PrincipalResolver.class);
        when(resolver.supports(any())).thenReturn(Boolean.FALSE);

        map.put(newMockHandler(true), resolver);
        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        val manager = getAuthenticationManager(authenticationExecutionPlan);
        assertThrows(UnresolvedPrincipalException.class, () -> manager.authenticate(transaction));

        when(resolver.supports(any())).thenReturn(Boolean.TRUE);
        when(resolver.resolve(any(), any(), any(), any(Optional.class))).thenThrow(new RuntimeException("Fails"));
        assertThrows(UnresolvedPrincipalException.class, () -> manager.authenticate(transaction));
    }

    @Test
    void verifyResolverFailsAsFatal() throws Throwable {
        val map = new HashMap<AuthenticationHandler, PrincipalResolver>();
        val resolver = mock(PrincipalResolver.class);
        when(resolver.supports(any())).thenReturn(Boolean.FALSE);

        map.put(newMockHandler(true), resolver);
        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        val manager = getAuthenticationManager(authenticationExecutionPlan);
        assertThrows(UnresolvedPrincipalException.class, () -> manager.authenticate(transaction));
    }

    @Test
    void verifyAuthWithNoCreds() throws Throwable {
        val map = new HashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(true), null);
        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        val manager = getAuthenticationManager(authenticationExecutionPlan);
        assertThrows(AuthenticationException.class, () -> manager.authenticate(CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction()));
    }

    @Test
    void verifyAuthenticateAnySuccess() throws Throwable {
        val map = new HashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(true), null);
        map.put(newMockHandler(false), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new AtLeastOneCredentialValidatedAuthenticationPolicy());
        val manager = getAuthenticationManager(authenticationExecutionPlan);

        val auth = manager.authenticate(transaction);
        assertEquals(1, auth.getSuccesses().size());
        assertEquals(2, auth.getCredentials().size());
    }

    @Test
    void verifyAuthenticateAnyButTryAllSuccess() throws Throwable {
        val map = new HashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(true), null);
        map.put(newMockHandler(false), null);
        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new AtLeastOneCredentialValidatedAuthenticationPolicy(true));
        val manager = getAuthenticationManager(authenticationExecutionPlan);
        assertThrows(AuthenticationException.class, () -> manager.authenticate(transaction));
    }

    @Test
    void verifyAuthenticateAnyFailure() throws Throwable {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(false), null);
        map.put(newMockHandler(false), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new AtLeastOneCredentialValidatedAuthenticationPolicy());
        val manager = getAuthenticationManager(authenticationExecutionPlan);

        assertThrows(AuthenticationException.class, () -> manager.authenticate(transaction));
    }

    @Test
    void verifyAuthenticateAnyFailureWithError() throws Throwable {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(false, true), null);
        map.put(newMockHandler(false, true), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new AtLeastOneCredentialValidatedAuthenticationPolicy());
        val manager = getAuthenticationManager(authenticationExecutionPlan);

        assertThrows(AuthenticationException.class, () -> manager.authenticate(transaction));
    }

    @Test
    void verifyAuthenticateAllSuccess() throws Throwable {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(true), null);
        map.put(newMockHandler(true), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new AllCredentialsValidatedAuthenticationPolicy());
        val manager = getAuthenticationManager(authenticationExecutionPlan);

        val auth = manager.authenticate(transaction);
        assertEquals(2, auth.getSuccesses().size());
        assertEquals(0, auth.getFailures().size());
        assertEquals(2, auth.getCredentials().size());
    }

    @Test
    void verifyAuthenticatePolicyFailsGeneric() throws Throwable {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(true), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        val policy = mock(AuthenticationPolicy.class);
        when(policy.isSatisfiedBy(any(), any(), any(), any()))
            .thenThrow(new GeneralSecurityException(new FailedLoginException()));
        authenticationExecutionPlan.registerAuthenticationPolicy(policy);
        val manager = getAuthenticationManager(authenticationExecutionPlan);

        assertThrows(AuthenticationException.class, () -> manager.authenticate(transaction));
    }

    @Test
    void verifyAuthenticatePolicyFails() throws Throwable {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(true), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        val policy = mock(AuthenticationPolicy.class);
        when(policy.isSatisfiedBy(any(), any(), any(), any()))
            .thenThrow(new IllegalArgumentException());
        authenticationExecutionPlan.registerAuthenticationPolicy(policy);
        val manager = getAuthenticationManager(authenticationExecutionPlan);

        assertThrows(AuthenticationException.class, () -> manager.authenticate(transaction));
    }

    @Test
    void verifyAuthenticateAllFailure() throws Throwable {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(false), null);
        map.put(newMockHandler(false), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new AllCredentialsValidatedAuthenticationPolicy());
        val manager = getAuthenticationManager(authenticationExecutionPlan);

        assertThrows(AuthenticationException.class, () -> manager.authenticate(transaction));
    }

    @Test
    void verifyAuthenticateRequiredHandlerSuccess() throws Throwable {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(HANDLER_A, true), null);
        map.put(newMockHandler(HANDLER_B, false), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new RequiredAuthenticationHandlerAuthenticationPolicy(HANDLER_A));
        val manager = getAuthenticationManager(authenticationExecutionPlan);


        val auth = manager.authenticate(transaction);
        assertEquals(1, auth.getSuccesses().size());
        assertEquals(2, auth.getCredentials().size());
    }

    @Test
    void verifyAuthenticateRequiredHandlerFailure() throws Throwable {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(HANDLER_A, true), null);
        map.put(newMockHandler(HANDLER_B, false), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new RequiredAuthenticationHandlerAuthenticationPolicy(HANDLER_B));
        val manager = getAuthenticationManager(authenticationExecutionPlan);

        assertThrows(AuthenticationException.class, () -> manager.authenticate(transaction));
    }

    @Test
    void verifyAuthenticateRequiredHandlerTryAllSuccess() throws Throwable {
        val map = new LinkedHashMap<AuthenticationHandler, PrincipalResolver>();
        map.put(newMockHandler(HANDLER_A, true), null);
        map.put(newMockHandler(HANDLER_B, false), null);

        val authenticationExecutionPlan = getAuthenticationExecutionPlan(map);
        authenticationExecutionPlan.registerAuthenticationPolicy(new RequiredAuthenticationHandlerAuthenticationPolicy(Set.of(HANDLER_A), true));
        val manager = getAuthenticationManager(authenticationExecutionPlan);

        val auth = manager.authenticate(transaction);
        assertEquals(1, auth.getSuccesses().size());
        assertEquals(1, auth.getFailures().size());
        assertEquals(2, auth.getCredentials().size());
    }

    private AuthenticationManager getAuthenticationManager(final AuthenticationEventExecutionPlan authenticationExecutionPlan) {
        return new DefaultAuthenticationManager(authenticationExecutionPlan,
            new DirectObjectProvider<>(CoreAuthenticationTestUtils.getAuthenticationSystemSupport()),
            false, applicationContext);
    }

    @TestConfiguration(value = "AuthenticationPlanTestConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class AuthenticationPlanTestConfiguration {
        @Bean
        public ServicesManager servicesManager() {
            return mockServicesManager();
        }
    }
    
}
