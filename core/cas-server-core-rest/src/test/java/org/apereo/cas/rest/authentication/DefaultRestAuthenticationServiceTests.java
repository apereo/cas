package org.apereo.cas.rest.authentication;

import module java.base;
import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.authentication.AuthenticationPolicyExecutionResult;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationCredential;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.attribute.AttributeRepositoryResolver;
import org.apereo.cas.authentication.attribute.StubPersonAttributeDao;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreRestAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.rest.BadRestRequestException;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.validation.AuthenticationContextValidationResult;
import org.apereo.cas.validation.RequestedAuthenticationContextValidator;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.LinkedMultiValueMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultRestAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("RestfulApiAuthentication")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreRestAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreCookieAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    DefaultRestAuthenticationServiceTests.AuthenticationTestConfiguration.class,
    CasCoreWebflowAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
class DefaultRestAuthenticationServiceTests {
    @Autowired
    @Qualifier(RestAuthenticationService.DEFAULT_BEAN_NAME)
    private RestAuthenticationService restAuthenticationService;

    @Test
    void verifyAuthentication() throws Throwable {
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("195.88.151.11");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
        
        val body = new LinkedMultiValueMap<String, String>();
        body.add(RestHttpRequestCredentialFactory.PARAMETER_USERNAME, "casuser");
        body.add(RestHttpRequestCredentialFactory.PARAMETER_PASSWORD, "Mellon");
        val result = restAuthenticationService.authenticate(body, request, response).orElseThrow();
        assertEquals("casuser", result.getAuthentication().getPrincipal().getId());
    }

    @Test
    void verifyMultifactorCredentialsFollowPrimaryAuthentication() throws Throwable {
        val authenticationSystemSupport = mock(AuthenticationSystemSupport.class);
        val credentialFactory = mock(RestHttpRequestCredentialFactory.class);
        val serviceFactory = mock(ServiceFactory.class);
        val triggerSelectionStrategy = mock(MultifactorAuthenticationTriggerSelectionStrategy.class);
        val servicesManager = mock(ServicesManager.class);
        val requestedContextValidator = mock(RequestedAuthenticationContextValidator.class);
        val authenticationPolicy = mock(AuthenticationPolicy.class);
        val applicationContext = mock(ConfigurableApplicationContext.class);
        val service = CoreAuthenticationTestUtils.getWebApplicationService();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val body = new LinkedMultiValueMap<String, String>();
        val primaryCredential = new UsernamePasswordCredential("claimed-user", "password");
        val multifactorCredential = new TestMultifactorCredential("untrusted-user");

        when(credentialFactory.fromRequest(request, body))
            .thenReturn(List.of(multifactorCredential))
            .thenReturn(List.of(primaryCredential, multifactorCredential));
        when(serviceFactory.createService(request)).thenReturn(service);

        val authentication = CoreAuthenticationTestUtils.getAuthentication("resolved-user");
        val authenticationResultBuilder = mock(AuthenticationResultBuilder.class);
        val authenticationResult = mock(AuthenticationResult.class);
        when(authenticationSystemSupport.handleInitialAuthenticationTransaction(eq(service), any(Credential[].class)))
            .thenAnswer(invocation -> {
                assertEquals(primaryCredential, invocation.getArgument(1));
                return authenticationResultBuilder;
            });
        when(authenticationResultBuilder.getInitialAuthentication()).thenReturn(Optional.of(authentication));
        when(authenticationPolicy.isSatisfiedBy(authentication, applicationContext))
            .thenReturn(AuthenticationPolicyExecutionResult.success());
        when(requestedContextValidator.validateAuthenticationContext(request, response, null, authentication, service))
            .thenReturn(AuthenticationContextValidationResult.builder().success(false).build());

        val provider = mock(MultifactorAuthenticationProvider.class);
        when(triggerSelectionStrategy.resolve(request, response, null, authentication, service)).thenReturn(Optional.of(provider));
        val multifactorCredentials = List.<Credential>of(new TestMultifactorCredential(authentication.getPrincipal().getId()));
        when(credentialFactory.fromAuthentication(request, body, authentication, provider)).thenReturn(multifactorCredentials);
        when(authenticationSystemSupport.handleAuthenticationTransaction(eq(service), same(authenticationResultBuilder),
            any(Credential[].class))).thenAnswer(invocation -> {
                assertEquals(multifactorCredentials.getFirst(), invocation.getArgument(2));
                return authenticationResultBuilder;
            });
        when(authenticationSystemSupport.finalizeAllAuthenticationTransactions(authenticationResultBuilder, service))
            .thenReturn(authenticationResult);

        val authenticationService = new DefaultRestAuthenticationService(authenticationSystemSupport, credentialFactory,
            serviceFactory, triggerSelectionStrategy, servicesManager, requestedContextValidator, authenticationPolicy, applicationContext);

        assertThrows(BadRestRequestException.class, () -> authenticationService.authenticate(body, request, response));
        verifyNoInteractions(authenticationSystemSupport);

        assertSame(authenticationResult, authenticationService.authenticate(body, request, response).orElseThrow());
        verify(authenticationSystemSupport).handleAuthenticationTransaction(eq(service),
            same(authenticationResultBuilder), any(Credential[].class));
        verify(authenticationSystemSupport).finalizeAllAuthenticationTransactions(authenticationResultBuilder, service);
        verify(authenticationSystemSupport, never()).finalizeAuthenticationTransaction(any(), anyCollection());
    }

    private static final class TestMultifactorCredential extends BasicIdentifiableCredential
        implements MultifactorAuthenticationCredential {
        @Serial
        private static final long serialVersionUID = -202165516679529385L;

        TestMultifactorCredential(final String id) {
            super(id);
        }

        @Override
        public String getProviderId() {
            return "mfa-test";
        }
    }

    @TestConfiguration(value = "AuthenticationTestConfiguration", proxyBeanMethods = false)
    static class AuthenticationTestConfiguration {
        @Bean
        public AuthenticationEventExecutionPlanConfigurer surrogateAuthenticationEventExecutionPlanConfigurer() {
            return plan -> plan.registerAuthenticationHandler(new AcceptUsersAuthenticationHandler(CollectionUtils.wrap("casuser", "Mellon")));
        }

        @Bean
        public PersonAttributeDao attributeRepository() {
            val attrs = CollectionUtils.wrap(
                "uid", CollectionUtils.wrap("uid"),
                "mail", CollectionUtils.wrap("cas@apereo.org"),
                "eduPersonAffiliation", CollectionUtils.wrap("developer"),
                "groupMembership", CollectionUtils.wrap("adopters"));
            return new StubPersonAttributeDao((Map) attrs);
        }

        @Bean
        public AttributeRepositoryResolver attributeRepositoryResolver() {
            return AttributeRepositoryResolver.allAttributeRepositories();
        }

    }
}
