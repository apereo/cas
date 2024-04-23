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
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.policy.AllAuthenticationHandlersSucceededAuthenticationPolicy;
import org.apereo.cas.authentication.policy.AllCredentialsValidatedAuthenticationPolicy;
import org.apereo.cas.authentication.policy.AtLeastOneCredentialValidatedAuthenticationPolicy;
import org.apereo.cas.authentication.policy.ExcludedAuthenticationHandlerAuthenticationPolicy;
import org.apereo.cas.authentication.policy.RequiredAuthenticationHandlerAuthenticationPolicy;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
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
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    BaseCasCoreTests.SharedTestConfiguration.AttributeRepositoryTestConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class
})
@Tag("AuthenticationPolicy")
class AuthenticationPolicyAwareServiceTicketValidationAuthorizerTests {
    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

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

    @Test
    void verifyAllAuthenticationHandlersSucceededAuthenticationPolicy() throws Throwable {
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
    void verifyAllCredentialsValidatedAuthenticationPolicy() throws Throwable {
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
    void verifyRequiredHandlerAuthenticationPolicy() throws Throwable {
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
    void verifyRequiredHandlerAuthenticationPolicyTryAll() throws Throwable {
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
    void verifyOperationWithHandlersAndAtLeastOneCredential() throws Throwable {
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
    void verifyOperationWithHandlersAndAtLeastOneCredentialMustTryAll() throws Throwable {
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
    void verifyOperationWithExcludedHandlers() throws Throwable {
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
        val plan = new DefaultAuthenticationEventExecutionPlan();
        plan.registerAuthenticationHandlers(authenticationHandlers);
        plan.registerAuthenticationPolicy(policy);
        return new AuthenticationPolicyAwareServiceTicketValidationAuthorizer(servicesManager, plan, applicationContext);
    }
}
