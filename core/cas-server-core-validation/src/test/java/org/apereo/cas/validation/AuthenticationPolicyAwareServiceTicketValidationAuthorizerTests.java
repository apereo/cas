package org.apereo.cas.validation;

import org.apereo.cas.BaseCasCoreTests;
import org.apereo.cas.TestOneTimePasswordAuthenticationHandler;
import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.credential.OneTimePasswordCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.DefaultAuthenticationHandlerResolver;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.policy.AllAuthenticationHandlersSucceededAuthenticationPolicy;
import org.apereo.cas.authentication.policy.AllCredentialsValidatedAuthenticationPolicy;
import org.apereo.cas.authentication.policy.AtLeastOneCredentialValidatedAuthenticationPolicy;
import org.apereo.cas.authentication.policy.ExcludedAuthenticationHandlerAuthenticationPolicy;
import org.apereo.cas.authentication.policy.RequiredAuthenticationHandlerAuthenticationPolicy;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AuthenticationPolicyAwareServiceTicketValidationAuthorizerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    BaseCasCoreTests.SharedTestConfiguration.AttributeRepositoryTestConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreEnvironmentBootstrapAutoConfiguration.class,
    CasCoreMultitenancyAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class
})
@Tag("AuthenticationPolicy")
@ExtendWith(CasTestExtension.class)
class AuthenticationPolicyAwareServiceTicketValidationAuthorizerTests {
    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(TenantExtractor.BEAN_NAME)
    private TenantExtractor tenantExtractor;
    
    private static Assertion getAssertion(final Map<Credential, ? extends AuthenticationHandler> handlers) {
        val assertion = mock(Assertion.class);
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val authentication = CoreAuthenticationTestUtils.getAuthenticationBuilder(principal, handlers,
            Map.of(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS,
                handlers.values().stream().map(AuthenticationHandler::getName).collect(Collectors.toList()))).build();
        when(assertion.getPrimaryAuthentication()).thenReturn(authentication);
        return assertion;
    }

    private static SimpleTestUsernamePasswordAuthenticationHandler getSimpleTestAuthenticationHandler() {
        return new SimpleTestUsernamePasswordAuthenticationHandler();
    }

    private static AcceptUsersAuthenticationHandler getAcceptUsersAuthenticationHandler() {
        return new AcceptUsersAuthenticationHandler(Map.of("casuser", "Mellon"));
    }

    private static OneTimePasswordCredential getOtpCredential() {
        return new OneTimePasswordCredential("test", "123456789");
    }

    private static TestOneTimePasswordAuthenticationHandler getTestOtpAuthenticationHandler() {
        return new TestOneTimePasswordAuthenticationHandler(Map.of("casuser", "123456789"));
    }

    @BeforeEach
    void before() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("223.456.789.000");
        request.setLocalAddr("223.456.789.100");
        request.addHeader(HttpHeaders.USER_AGENT, "Firefox");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
    }
    
    @Test
    void verifyAllAuthenticationHandlersSucceededAuthenticationPolicy() {
        val handlers = List.of(getTestOtpAuthenticationHandler(), getAcceptUsersAuthenticationHandler(), getSimpleTestAuthenticationHandler());
        val service = CoreAuthenticationTestUtils.getService("https://example.com/high/");
        val authz = getAuthorizer(new AllAuthenticationHandlersSucceededAuthenticationPolicy(), handlers);
        val map = (Map) Map.of(
            new UsernamePasswordCredential(), getAcceptUsersAuthenticationHandler(),
            getOtpCredential(), getTestOtpAuthenticationHandler());
        val assertion = getAssertion(map);
        assertDoesNotThrow(() -> authz.authorize(new MockHttpServletRequest(), service, assertion));
    }

    @Test
    void verifyAllCredentialsValidatedAuthenticationPolicy() {
        val handlers = List.of(getTestOtpAuthenticationHandler(), getAcceptUsersAuthenticationHandler(), getSimpleTestAuthenticationHandler());
        val service = CoreAuthenticationTestUtils.getService("https://example.com/high/");
        val authz = getAuthorizer(new AllCredentialsValidatedAuthenticationPolicy(), handlers);
        val map = (Map) Map.of(
            new UsernamePasswordCredential(), getAcceptUsersAuthenticationHandler(),
            getOtpCredential(), getTestOtpAuthenticationHandler());
        val assertion = getAssertion(map);
        assertDoesNotThrow(() -> authz.authorize(new MockHttpServletRequest(), service, assertion));
    }

    @Test
    void verifyRequiredHandlerAuthenticationPolicy() {
        val handler = getAcceptUsersAuthenticationHandler();
        val handlers = List.of(getTestOtpAuthenticationHandler(), handler, getSimpleTestAuthenticationHandler());
        val service = CoreAuthenticationTestUtils.getService("https://example.com/high/");
        val authz = getAuthorizer(new RequiredAuthenticationHandlerAuthenticationPolicy(handler.getName()), handlers);
        val map = (Map) Map.of(
            new UsernamePasswordCredential(), handler,
            getOtpCredential(), getTestOtpAuthenticationHandler());
        val assertion = getAssertion(map);
        assertDoesNotThrow(() -> authz.authorize(new MockHttpServletRequest(), service, assertion));
    }

    @Test
    void verifyRequiredHandlerAuthenticationPolicyTryAll() {
        val handler = getAcceptUsersAuthenticationHandler();
        val handlers = List.of(getTestOtpAuthenticationHandler(), handler, getSimpleTestAuthenticationHandler());
        val service = CoreAuthenticationTestUtils.getService("https://example.com/high/");
        val authz = getAuthorizer(new RequiredAuthenticationHandlerAuthenticationPolicy(Set.of(handler.getName()), true), handlers);
        val map = (Map) Map.of(
            new UsernamePasswordCredential(), handler,
            getOtpCredential(), getTestOtpAuthenticationHandler());
        val assertion = getAssertion(map);
        assertDoesNotThrow(() -> authz.authorize(new MockHttpServletRequest(), service, assertion));
    }

    @Test
    void verifyOperationWithHandlersAndAtLeastOneCredential() {
        val handlers = List.of(getTestOtpAuthenticationHandler(), getAcceptUsersAuthenticationHandler(), getSimpleTestAuthenticationHandler());
        val service = CoreAuthenticationTestUtils.getService("https://example.com/high/");
        val authz = getAuthorizer(new AtLeastOneCredentialValidatedAuthenticationPolicy(), handlers);
        val map = (Map) Map.of(
            new UsernamePasswordCredential(), getAcceptUsersAuthenticationHandler(),
            getOtpCredential(), getTestOtpAuthenticationHandler());
        val assertion = getAssertion(map);
        assertDoesNotThrow(() -> authz.authorize(new MockHttpServletRequest(), service, assertion));
    }

    @Test
    void verifyOperationWithHandlersAndAtLeastOneCredentialMustTryAll() {
        val handlers = List.of(getTestOtpAuthenticationHandler(), getAcceptUsersAuthenticationHandler(), getSimpleTestAuthenticationHandler());
        val service = CoreAuthenticationTestUtils.getService("https://example.com/high/");
        val authz = getAuthorizer(new AtLeastOneCredentialValidatedAuthenticationPolicy(true), handlers);
        val map = (Map) Map.of(
            new UsernamePasswordCredential(), getAcceptUsersAuthenticationHandler(),
            getOtpCredential(), getTestOtpAuthenticationHandler());
        val assertion = getAssertion(map);
        assertDoesNotThrow(() -> authz.authorize(new MockHttpServletRequest(), service, assertion));
    }

    @Test
    void verifyOperationWithExcludedHandlers() {
        val h1 = getTestOtpAuthenticationHandler();
        val h2 = getSimpleTestAuthenticationHandler();
        val handlers = List.of(h1, getAcceptUsersAuthenticationHandler(), h2);
        val service = CoreAuthenticationTestUtils.getService("https://example.com/high/");
        val authz = getAuthorizer(new ExcludedAuthenticationHandlerAuthenticationPolicy(Set.of(h1.getName(), h2.getName()), false), handlers);
        val map = (Map) Map.of(
            new UsernamePasswordCredential(), getAcceptUsersAuthenticationHandler(),
            getOtpCredential(), h1);
        val assertion = getAssertion(map);
        assertThrows(UnauthorizedServiceException.class, () -> authz.authorize(new MockHttpServletRequest(), service, assertion));
    }

    private ServiceTicketValidationAuthorizer getAuthorizer(final AuthenticationPolicy policy,
                                                            final List<? extends AuthenticationHandler> authenticationHandlers) {
        val plan = new DefaultAuthenticationEventExecutionPlan(new DefaultAuthenticationHandlerResolver(), tenantExtractor);
        plan.registerAuthenticationHandlers(authenticationHandlers);
        plan.registerAuthenticationPolicy(policy);
        return new AuthenticationPolicyAwareServiceTicketValidationAuthorizer(servicesManager, plan, applicationContext);
    }
}
