package org.apereo.cas.validation;

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
import org.apereo.cas.authentication.policy.RequiredHandlerAuthenticationPolicy;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
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
    CasPersonDirectoryTestConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    MailSenderAutoConfiguration.class
})
@Tag("Simple")
public class AuthenticationPolicyAwareServiceTicketValidationAuthorizerTests {
    @Autowired
    @Qualifier("servicesManager")
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
    public void verifyAllAuthenticationHandlersSucceededAuthenticationPolicy() {
        val handlers = List.of(getTestOtpAuthenticationHandler(), getAcceptUsersAuthenticationHandler(), getSimpleTestAuthenticationHandler());
        val service = CoreAuthenticationTestUtils.getService("https://example.com/high/");
        val authz = getAuthorizer(new AllAuthenticationHandlersSucceededAuthenticationPolicy(), handlers);
        val map = (Map) Map.of(
            new UsernamePasswordCredential(), getAcceptUsersAuthenticationHandler(),
            getOtpCredential(), getTestOtpAuthenticationHandler());
        val assertion = getAssertion(map);
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                authz.authorize(new MockHttpServletRequest(), service, assertion);
            }
        });
    }

    @Test
    public void verifyAllCredentialsValidatedAuthenticationPolicy() {
        val handlers = List.of(getTestOtpAuthenticationHandler(), getAcceptUsersAuthenticationHandler(), getSimpleTestAuthenticationHandler());
        val service = CoreAuthenticationTestUtils.getService("https://example.com/high/");
        val authz = getAuthorizer(new AllCredentialsValidatedAuthenticationPolicy(), handlers);
        val map = (Map) Map.of(
            new UsernamePasswordCredential(), getAcceptUsersAuthenticationHandler(),
            getOtpCredential(), getTestOtpAuthenticationHandler());
        val assertion = getAssertion(map);
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                authz.authorize(new MockHttpServletRequest(), service, assertion);
            }
        });
    }

    @Test
    public void verifyRequiredHandlerAuthenticationPolicy() {
        val handler = getAcceptUsersAuthenticationHandler();
        val handlers = List.of(getTestOtpAuthenticationHandler(), handler, getSimpleTestAuthenticationHandler());
        val service = CoreAuthenticationTestUtils.getService("https://example.com/high/");
        val authz = getAuthorizer(new RequiredHandlerAuthenticationPolicy(handler.getName()), handlers);
        val map = (Map) Map.of(
            new UsernamePasswordCredential(), handler,
            getOtpCredential(), getTestOtpAuthenticationHandler());
        val assertion = getAssertion(map);
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                authz.authorize(new MockHttpServletRequest(), service, assertion);
            }
        });
    }

    @Test
    public void verifyRequiredHandlerAuthenticationPolicyTryAll() {
        val handler = getAcceptUsersAuthenticationHandler();
        val handlers = List.of(getTestOtpAuthenticationHandler(), handler, getSimpleTestAuthenticationHandler());
        val service = CoreAuthenticationTestUtils.getService("https://example.com/high/");
        val authz = getAuthorizer(new RequiredHandlerAuthenticationPolicy(Set.of(handler.getName()), true), handlers);
        val map = (Map) Map.of(
            new UsernamePasswordCredential(), handler,
            getOtpCredential(), getTestOtpAuthenticationHandler());
        val assertion = getAssertion(map);
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                authz.authorize(new MockHttpServletRequest(), service, assertion);
            }
        });
    }

    @Test
    public void verifyOperationWithHandlersAndAtLeastOneCredential() {
        val handlers = List.of(getTestOtpAuthenticationHandler(), getAcceptUsersAuthenticationHandler(), getSimpleTestAuthenticationHandler());
        val service = CoreAuthenticationTestUtils.getService("https://example.com/high/");
        val authz = getAuthorizer(new AtLeastOneCredentialValidatedAuthenticationPolicy(), handlers);
        val map = (Map) Map.of(
            new UsernamePasswordCredential(), getAcceptUsersAuthenticationHandler(),
            getOtpCredential(), getTestOtpAuthenticationHandler());
        val assertion = getAssertion(map);
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                authz.authorize(new MockHttpServletRequest(), service, assertion);
            }
        });
    }

    @Test
    public void verifyOperationWithHandlersAndAtLeastOneCredentialMustTryAll() {
        val handlers = List.of(getTestOtpAuthenticationHandler(), getAcceptUsersAuthenticationHandler(), getSimpleTestAuthenticationHandler());
        val service = CoreAuthenticationTestUtils.getService("https://example.com/high/");
        val authz = getAuthorizer(new AtLeastOneCredentialValidatedAuthenticationPolicy(true), handlers);
        val map = (Map) Map.of(
            new UsernamePasswordCredential(), getAcceptUsersAuthenticationHandler(),
            getOtpCredential(), getTestOtpAuthenticationHandler());
        val assertion = getAssertion(map);
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                authz.authorize(new MockHttpServletRequest(), service, assertion);
            }
        });
    }

    private ServiceTicketValidationAuthorizer getAuthorizer(final AuthenticationPolicy policy,
                                                            final List<? extends AuthenticationHandler> authenticationHandlers) {
        val plan = new DefaultAuthenticationEventExecutionPlan();
        plan.registerAuthenticationHandlers(authenticationHandlers);
        plan.registerAuthenticationPolicy(policy);
        return new AuthenticationPolicyAwareServiceTicketValidationAuthorizer(servicesManager, plan, applicationContext);
    }
}
