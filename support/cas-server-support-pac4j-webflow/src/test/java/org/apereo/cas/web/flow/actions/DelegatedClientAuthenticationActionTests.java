package org.apereo.cas.web.flow.actions;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationCandidateProfile;
import org.apereo.cas.authentication.principal.DelegatedClientAuthenticationCredentialResolver;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.TestBaseDelegatedClientAuthenticationCredentialResolver;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.AllAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceAuthenticationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.MockServletContext;
import org.apereo.cas.util.http.HttpRequestUtils;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This class tests the {@link DelegatedClientAuthenticationAction} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
@Tag("Delegation")
@Slf4j
class DelegatedClientAuthenticationActionTests {

    @TestConfiguration(proxyBeanMethods = false)
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
            val context = MockRequestContext.create();
            context.getHttpServletRequest().addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Chrome");
            context.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FacebookClient");

            val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));
            context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            val client = builtClients.findClient("FacebookClient").get();
            val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            context.setParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());
            val event = delegatedAuthenticationAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SELECT, event.getId());
        }

        @Test
        void verifyCredentialSelectionFinish() throws Throwable {

            val context = MockRequestContext.create();
            context.getHttpServletRequest().addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Chrome");
            context.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FacebookClient");

            val client = builtClients.findClient("FacebookClient").get();
            val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());
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
            val request = new MockHttpServletRequest();
            request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Chrome");
            val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId()));
            request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            var response = new MockHttpServletResponse();
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            val context = new MockRequestContext();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            val client = builtClients.findClient("FacebookClient").get();
            val webContext = new JEEContext(request, response);
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            request.setParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

            val event = delegatedAuthenticationAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_STOP, event.getId());
        }

        @Test
        void verifyFinishAuthenticationAuthzFailure() throws Throwable {
            val request = new MockHttpServletRequest();
            request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Chrome");
            request.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FacebookClient");
            val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
            request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            val context = new MockRequestContext();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            val client = builtClients.findClient("FacebookClient").get();
            val webContext = new JEEContext(request, new MockHttpServletResponse());
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            request.setParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

            assertThrows(UnauthorizedServiceException.class,
                () -> delegatedAuthenticationAction.execute(context));
        }

        @Test
        void verifySaml2LogoutResponse() throws Throwable {
            val client = builtClients.findClient("SAML2Client").get();

            val request = new MockHttpServletRequest();
            request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Chrome");
            request.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, client.getName());
            val webContext = new JEEContext(request, new MockHttpServletResponse());
            request.setMethod("POST");

            val logoutResponse = getLogoutResponse();
            request.setContent(EncodingUtils.encodeBase64(logoutResponse).getBytes(StandardCharsets.UTF_8));

            val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));
            request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            val context = new MockRequestContext();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            request.setParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

            val event = delegatedAuthenticationAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_LOGOUT, event.getId());
        }

        @Test
        void verifyFinishAuthentication() throws Throwable {
            val request = new MockHttpServletRequest();
            request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Chrome");
            request.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FacebookClient");

            request.setParameter(ThemeChangeInterceptor.DEFAULT_PARAM_NAME, "theme");
            request.setParameter(LocaleChangeInterceptor.DEFAULT_PARAM_NAME, Locale.getDefault().getCountry());
            request.setParameter(CasProtocolConstants.PARAMETER_METHOD, HttpMethod.POST.name());
            val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));
            request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            val context = new MockRequestContext();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            val client = builtClients.findClient("FacebookClient").get();
            val webContext = new JEEContext(request, new MockHttpServletResponse());
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            request.setParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

            val event = delegatedAuthenticationAction.execute(context);

            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
            assertEquals("theme", request.getAttribute(ThemeChangeInterceptor.DEFAULT_PARAM_NAME));
            assertEquals(Locale.getDefault().getCountry(), request.getAttribute(LocaleChangeInterceptor.DEFAULT_PARAM_NAME));
            assertEquals(HttpMethod.POST.name(), request.getAttribute(CasProtocolConstants.PARAMETER_METHOD));
            assertEquals(service.getId(), ((Principal) request.getAttribute(CasProtocolConstants.PARAMETER_SERVICE)).getId());
            val flowScope = context.getFlowScope();
            assertEquals(service.getId(), flowScope.get(CasProtocolConstants.PARAMETER_SERVICE, Service.class).getId());
            val credential = flowScope.get(CasWebflowConstants.VAR_ID_CREDENTIAL, ClientCredential.class);
            assertNotNull(credential);
            assertEquals("casuser", credential.getId());
        }

        @Test
        void verifyFailedAuthentication() throws Throwable {
            val mockRequest = new MockHttpServletRequest();
            mockRequest.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Chrome");
            mockRequest.setParameter("error_message", "bad authn");
            mockRequest.setParameter("error_code", "403");
            mockRequest.setParameter("error_description", "authentication failed");
            val service = CoreAuthenticationTestUtils.getService();
            mockRequest.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            val servletExternalContext = mock(ServletExternalContext.class);
            when(servletExternalContext.getNativeRequest()).thenReturn(mockRequest);
            when(servletExternalContext.getNativeResponse()).thenReturn(new MockHttpServletResponse());

            val mockRequestContext = new MockRequestContext();
            mockRequestContext.setExternalContext(servletExternalContext);

            assertEquals(CasWebflowConstants.TRANSITION_ID_STOP, delegatedAuthenticationAction.execute(mockRequestContext).getId());
        }

        @Test
        void verifySsoAuthenticationWithUnauthorizedSso() throws Throwable {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();

            request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Chrome");
            val client = builtClients.findClient("FacebookClient").orElse(null);
            val webContext = new JEEContext(request, new MockHttpServletResponse());

            request.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FacebookClient");
            val service = CoreAuthenticationTestUtils.getService("https://delegated2-authn-policy.example.org");
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of());
            val authenticationPolicy = new DefaultRegisteredServiceAuthenticationPolicy();
            authenticationPolicy.setRequiredAuthenticationHandlers(Set.of("DelegatedClientAuthenticationHandler"));
            authenticationPolicy.setCriteria(new AllAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria());
            registeredService.setAuthenticationPolicy(authenticationPolicy);
            servicesManager.save(registeredService);
            request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            request.setParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            val tgt = new MockTicketGrantingTicket("casuser");
            ticketRegistry.addTicket(tgt);
            WebUtils.putTicketGrantingTicketInScopes(context, tgt);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, delegatedAuthenticationAction.execute(context).getId());
            assertThrows(InvalidTicketException.class, () -> ticketRegistry.getTicket(tgt.getId(), TicketGrantingTicket.class));
        }

        @Test
        void verifySsoAuthentication() throws Throwable {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();

            request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Chrome");
            val client = builtClients.findClient("FacebookClient").orElse(null);
            val webContext = new JEEContext(request, new MockHttpServletResponse());
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            request.setParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

            request.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FacebookClient");
            val service = CoreAuthenticationTestUtils.getService("https://delegated2.example.org");
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));
            request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            val tgt = new MockTicketGrantingTicket("casuser");
            ticketRegistry.addTicket(tgt);
            WebUtils.putTicketGrantingTicketInScopes(context, tgt);
            assertEquals(CasWebflowConstants.TRANSITION_ID_GENERATE_SERVICE_TICKET, delegatedAuthenticationAction.execute(context).getId());
        }

        @Test
        void verifySsoAuthenticationWithInvalidTicketFails() throws Throwable {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            request.setParameter("error_message", "Auth+failed");
            val response = new MockHttpServletResponse();

            request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Chrome");
            val client = builtClients.findClient("FacebookClient").get();
            val webContext = new JEEContext(request, new MockHttpServletResponse());
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            request.setParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

            request.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FacebookClient");
            val service = CoreAuthenticationTestUtils.getService("https://delegated2.example.org");
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));
            request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            val tgt = new MockTicketGrantingTicket("casuser");
            ticketRegistry.addTicket(tgt);
            WebUtils.putTicketGrantingTicketInScopes(context, new MockTicketGrantingTicket("otheruser"));
            assertEquals(CasWebflowConstants.TRANSITION_ID_STOP, delegatedAuthenticationAction.execute(context).getId());
        }

        @Test
        void verifyLogoutRequestWithOkAction() throws Throwable {
            val request = new MockHttpServletRequest();
            request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Chrome");
            request.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "LogoutClient");
            val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));
            request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            val context = new MockRequestContext();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            val event = delegatedAuthenticationAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_LOGOUT, event.getId());
        }

        @Test
        void verifyServerSideRedirectAuthentication() throws Throwable {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Chrome");
            val response = new MockHttpServletResponse();

            val service = CoreAuthenticationTestUtils.getService("https://delegated2.example.org");
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));
            request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            assertEquals(CasWebflowConstants.TRANSITION_ID_GENERATE, delegatedAuthenticationAction.execute(context).getId());

            val generated = delegatedAuthenticationCreateClientsAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, generated.getId());

            assertEquals(HttpStatus.FOUND.value(), response.getStatus());
            assertEquals(DelegatedClientIdentityProviderConfigurationFactory.ENDPOINT_URL_REDIRECT + "?client_name=CasClient",
                response.getHeader("Location"));
        }

        @Test
        void verifySsoAuthenticationUnauthz() throws Throwable {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Chrome");
            val response = new MockHttpServletResponse();

            request.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FacebookClient");
            val service = CoreAuthenticationTestUtils.getService("https://delegated3.example.org");
            request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
            val client = builtClients.findClient("FacebookClient").get();
            val webContext = new JEEContext(request, new MockHttpServletResponse());
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            request.setParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

            val accessStrategy = new DefaultRegisteredServiceAccessStrategy();
            accessStrategy.setEnabled(false);
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());
            registeredService.setAccessStrategy(accessStrategy);
            servicesManager.save(registeredService);

            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            val tgt = new MockTicketGrantingTicket("casuser", Map.of(),
                Map.of(ClientCredential.AUTHENTICATION_ATTRIBUTE_CLIENT_NAME, List.of("FacebookClient")));
            ticketRegistry.addTicket(tgt);
            WebUtils.putTicketGrantingTicketInScopes(context, tgt);

            assertThrows(UnauthorizedServiceException.class, () -> delegatedAuthenticationAction.execute(context).getId());
            assertThrows(InvalidTicketException.class, () -> ticketRegistry.getTicket(tgt.getId(), TicketGrantingTicket.class));
        }
    }
}
