package org.apereo.cas.web.flow.actions;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationCandidateProfile;
import org.apereo.cas.authentication.principal.DelegatedClientAuthenticationCredentialResolver;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.TestBaseDelegatedClientAuthenticationCredentialResolver;
import org.apereo.cas.logout.slo.SingleLogoutContinuation;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.AllAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceAuthenticationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfigurationFactory;
import org.apereo.cas.web.flow.BaseDelegatedClientAuthenticationActionTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowManager;
import org.apereo.cas.web.flow.DelegationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests the {@link DelegatedClientAuthenticationAction} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
@Tag("Delegation")
@Slf4j
class DelegatedClientAuthenticationActionTests {

    @TestConfiguration(value = "CredentialTestConfiguration", proxyBeanMethods = false)
    static class CredentialTestConfiguration {
        @Bean
        public DelegatedClientAuthenticationCredentialResolver testDelegatedCredentialResolver(
            @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME) final DelegatedClientAuthenticationConfigurationContext configurationContext) {
            return new TestBaseDelegatedClientAuthenticationCredentialResolver(configurationContext);
        }
    }

    @Import(CredentialTestConfiguration.class)
    @Nested
    class CredentialSelectionTests extends BaseDelegatedClientAuthenticationActionTests {

        @Test
        void verifyCredentialSelectionStart() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.withUserAgent();
            context.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FakeClient");
            val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());

            val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));
            context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            val client = identityProviders.findClient("FakeClient", webContext).orElseThrow();
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            context.setParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());
            val event = delegatedAuthenticationAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SELECT, event.getId());
        }

        @Test
        void verifyCredentialSelectionFinish() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.withUserAgent();
            context.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FakeClient");

            val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());
            val client = identityProviders.findClient("FakeClient", webContext).orElseThrow();
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            context.setParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());
            val p1 = DelegatedAuthenticationCandidateProfile.builder()
                .id("casuser")
                .key(UUID.randomUUID().toString())
                .linkedId("casuser-linked")
                .build();
            DelegationWebflowUtils.putDelegatedClientAuthenticationCandidateProfile(context, p1);
            val event = delegatedAuthenticationAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        }
    }

    @Nested
    class DefaultTests extends BaseDelegatedClientAuthenticationActionTests {
        @Test
        void verifyStartAuthenticationNoService() throws Throwable {
            assertStartAuthentication(null);
        }

        @Test
        void verifyStartAuthenticationWithService() throws Throwable {
            val service = RegisteredServiceTestUtils.getService(RegisteredServiceTestUtils.CONST_TEST_URL);
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId()));
            assertStartAuthentication(service);
        }

        @Test
        void verifyExecutionFailureWithUnauthzResponse() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            context.withUserAgent();
            val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId()));
            context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            context.getHttpServletResponse().setStatus(HttpStatus.UNAUTHORIZED.value());
            val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());
            val client = identityProviders.findClient("FakeClient", webContext).orElseThrow();
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            context.setParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

            val event = delegatedAuthenticationAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_STOP, event.getId());
        }

        @Test
        void verifyFinishAuthenticationAuthzFailure() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            context.withUserAgent();
            context.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FakeClient");
            val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
            context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());
            val client = identityProviders.findClient("FakeClient", webContext).orElseThrow();
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            context.setParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

            assertThrows(UnauthorizedServiceException.class, () -> delegatedAuthenticationAction.execute(context));
        }

        @Test
        void verifyFinishAuthentication() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            context.withUserAgent();
            context.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FakeClient");

            context.setParameter(ThemeChangeInterceptor.DEFAULT_PARAM_NAME, "theme");
            context.setParameter(LocaleChangeInterceptor.DEFAULT_PARAM_NAME, Locale.getDefault().getCountry());
            context.setParameter(CasProtocolConstants.PARAMETER_METHOD, HttpMethod.POST.name());
            val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));
            context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());
            val client = identityProviders.findClient("FakeClient", webContext).orElseThrow();
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            context.setParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

            val event = delegatedAuthenticationAction.execute(context);

            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
            assertEquals("theme", context.getHttpServletRequest().getAttribute(ThemeChangeInterceptor.DEFAULT_PARAM_NAME));
            assertEquals(Locale.getDefault().getCountry(), context.getHttpServletRequest().getAttribute(LocaleChangeInterceptor.DEFAULT_PARAM_NAME));
            assertEquals(HttpMethod.POST.name(), context.getHttpServletRequest().getAttribute(CasProtocolConstants.PARAMETER_METHOD));
            assertEquals(service.getId(), ((Service) context.getHttpServletRequest().getAttribute(CasProtocolConstants.PARAMETER_SERVICE)).getId());
            val flowScope = context.getFlowScope();
            assertEquals(service.getId(), flowScope.get(CasProtocolConstants.PARAMETER_SERVICE, Service.class).getId());
            val credential = flowScope.get(CasWebflowConstants.VAR_ID_CREDENTIAL, ClientCredential.class);
            assertNotNull(credential);
            assertEquals("casuser", credential.getId());
        }

        @Test
        void verifyStopWebflowOnCredentialFailure() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            context.withUserAgent();
            context.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "BadCredentialsClient");

            context.setParameter(ThemeChangeInterceptor.DEFAULT_PARAM_NAME, "theme");
            context.setParameter(LocaleChangeInterceptor.DEFAULT_PARAM_NAME, Locale.getDefault().getCountry());
            context.setParameter(CasProtocolConstants.PARAMETER_METHOD, HttpMethod.POST.name());
            val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));
            context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());
            val client = identityProviders.findClient("BadCredentialsClient", webContext).orElseThrow();
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            context.setParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

            assertEquals(CasWebflowConstants.TRANSITION_ID_STOP, delegatedAuthenticationAction.execute(context).getId());
        }

        @Test
        void verifyFailedAuthentication() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            context.withUserAgent();
            context.setParameter("error_message", "bad authn");
            context.setParameter("error_code", "403");
            context.setParameter("error_description", "authentication failed");
            val service = CoreAuthenticationTestUtils.getService();
            context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            assertEquals(CasWebflowConstants.TRANSITION_ID_STOP, delegatedAuthenticationAction.execute(context).getId());
        }

        @Test
        void verifySsoAuthenticationWithUnauthorizedSso() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            context.withUserAgent();
            val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());
            val client = identityProviders.findClient("FakeClient", webContext).orElseThrow();

            context.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FakeClient");
            val service = CoreAuthenticationTestUtils.getService("https://delegated2-authn-policy.example.org");
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of());
            val authenticationPolicy = new DefaultRegisteredServiceAuthenticationPolicy();
            authenticationPolicy.setRequiredAuthenticationHandlers(Set.of("DelegatedClientAuthenticationHandler"));
            authenticationPolicy.setCriteria(new AllAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria());
            registeredService.setAuthenticationPolicy(authenticationPolicy);
            servicesManager.save(registeredService);
            context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            context.setParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

            val tgt = new MockTicketGrantingTicket("casuser");
            ticketRegistry.addTicket(tgt);
            WebUtils.putTicketGrantingTicketInScopes(context, tgt);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, delegatedAuthenticationAction.execute(context).getId());
            assertThrows(InvalidTicketException.class, () -> ticketRegistry.getTicket(tgt.getId(), TicketGrantingTicket.class));
        }

        @Test
        void verifySsoAuthentication() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            context.withUserAgent();
            val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());
            val client = identityProviders.findClient("FakeClient", webContext).orElse(null);
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            context.setParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

            context.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FakeClient");
            val service = CoreAuthenticationTestUtils.getService("https://delegated2.example.org");
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));
            context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            val tgt = new MockTicketGrantingTicket("casuser");
            ticketRegistry.addTicket(tgt);
            WebUtils.putTicketGrantingTicketInScopes(context, tgt);
            assertEquals(CasWebflowConstants.TRANSITION_ID_GENERATE_SERVICE_TICKET, delegatedAuthenticationAction.execute(context).getId());
        }

        @Test
        void verifySsoAuthenticationWithInvalidTicketFails() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setParameter("error_message", "Auth+failed");

            context.withUserAgent();
            val webContext = new JEEContext(context.getHttpServletRequest(), new MockHttpServletResponse());
            val client = identityProviders.findClient("FakeClient", webContext).get();
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            context.setParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

            context.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FakeClient");
            val service = CoreAuthenticationTestUtils.getService("https://delegated2.example.org");
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));
            context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            val tgt = new MockTicketGrantingTicket("casuser");
            ticketRegistry.addTicket(tgt);
            WebUtils.putTicketGrantingTicketInScopes(context, new MockTicketGrantingTicket("otheruser"));
            assertEquals(CasWebflowConstants.TRANSITION_ID_STOP, delegatedAuthenticationAction.execute(context).getId());
        }

        @Test
        void verifyLogoutRequestWithOkAction() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.withUserAgent();
            context.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "LogoutClient");
            val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));
            context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            val event = delegatedAuthenticationAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_LOGOUT, event.getId());
            val continuation = (SingleLogoutContinuation) context.getHttpServletRequest().getAttribute(SingleLogoutContinuation.class.getName());
            assertNotNull(continuation);
            assertNotNull(continuation.getContent());
            assertNull(continuation.getUrl());
        }

        @Test
        void verifyLogoutRequestWithFormPost() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.withUserAgent();
            context.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "AutomaticPostLogoutClient");
            val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));
            context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            val event = delegatedAuthenticationAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_LOGOUT, event.getId());
            val continuation = (SingleLogoutContinuation) context.getHttpServletRequest().getAttribute(SingleLogoutContinuation.class.getName());
            assertNotNull(continuation);
            assertNull(continuation.getContent());
            assertNotNull(continuation.getUrl());
        }

        @Test
        void verifyServerSideRedirectAuthentication() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.withUserAgent();

            val service = CoreAuthenticationTestUtils.getService("https://delegated2.example.org");
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));
            context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            assertEquals(CasWebflowConstants.TRANSITION_ID_GENERATE, delegatedAuthenticationAction.execute(context).getId());

            val generated = delegatedAuthenticationCreateClientsAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, generated.getId());

            assertEquals(HttpStatus.FOUND.value(), context.getHttpServletResponse().getStatus());
            assertEquals(DelegatedClientIdentityProviderConfigurationFactory.ENDPOINT_URL_REDIRECT + "?client_name=CasClient",
                context.getHttpServletResponse().getHeader("Location"));
        }

        @Test
        void verifySsoAuthenticationUnauthz() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.withUserAgent();

            context.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FakeClient");
            val service = CoreAuthenticationTestUtils.getService("https://delegated3.example.org");
            context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
            val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());
            val client = identityProviders.findClient("FakeClient", webContext).get();
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            context.setParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

            val accessStrategy = new DefaultRegisteredServiceAccessStrategy();
            accessStrategy.setEnabled(false);
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());
            registeredService.setAccessStrategy(accessStrategy);
            servicesManager.save(registeredService);

            val tgt = new MockTicketGrantingTicket("casuser", Map.of(),
                Map.of(ClientCredential.AUTHENTICATION_ATTRIBUTE_CLIENT_NAME, List.of("FakeClient")));
            ticketRegistry.addTicket(tgt);
            WebUtils.putTicketGrantingTicketInScopes(context, tgt);

            assertThrows(UnauthorizedServiceException.class, () -> delegatedAuthenticationAction.execute(context).getId());
            assertThrows(InvalidTicketException.class, () -> ticketRegistry.getTicket(tgt.getId(), TicketGrantingTicket.class));
        }
    }
}
