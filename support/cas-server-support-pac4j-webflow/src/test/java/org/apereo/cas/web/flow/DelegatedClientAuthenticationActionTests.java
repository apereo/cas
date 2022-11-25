package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationCandidateProfile;
import org.apereo.cas.authentication.principal.DelegatedClientAuthenticationCredentialResolver;
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
import org.apereo.cas.util.MockServletContext;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfigurationFactory;
import org.apereo.cas.web.flow.actions.DelegatedClientAuthenticationAction;
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
import org.springframework.webflow.test.MockRequestContext;

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
@Tag("WebflowAuthenticationActions")
@Slf4j
public class DelegatedClientAuthenticationActionTests {


    @TestConfiguration(proxyBeanMethods = false)
    public static class CredentialTestConfiguration {
        @Bean
        public DelegatedClientAuthenticationCredentialResolver testDelegatedCredentialResolver(
            @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME)
            final DelegatedClientAuthenticationConfigurationContext configurationContext) {
            return new TestBaseDelegatedClientAuthenticationCredentialResolver(configurationContext);
        }
    }

    @Import(CredentialTestConfiguration.class)
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class CredentialSelectionTests extends BaseDelegatedClientAuthenticationActionTests {

        @Test
        public void verifyCredentialSelectionStart() throws Exception {
            val request = new MockHttpServletRequest();
            request.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FacebookClient");

            val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));
            request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            val context = new MockRequestContext();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            val client = builtClients.findClient("FacebookClient").get();
            val webContext = new JEEContext(request, new MockHttpServletResponse());
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            request.addParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());
            val event = delegatedAuthenticationAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SELECT, event.getId());
        }

        @Test
        public void verifyCredentialSelectionFinish() throws Exception {
            val request = new MockHttpServletRequest();
            request.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FacebookClient");

            val context = new MockRequestContext();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            val client = builtClients.findClient("FacebookClient").get();
            val webContext = new JEEContext(request, new MockHttpServletResponse());
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            request.addParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());
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
    @SuppressWarnings("ClassCanBeStatic")
    public class DefaultTests extends BaseDelegatedClientAuthenticationActionTests {
        @Test
        public void verifyStartAuthenticationNoService() throws Exception {
            assertStartAuthentication(null);
        }

        @Test
        public void verifyStartAuthenticationWithService() throws Exception {
            val service = RegisteredServiceTestUtils.getService(RegisteredServiceTestUtils.CONST_TEST_URL);
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId()));
            assertStartAuthentication(service);
        }

        @Test
        public void verifyExecutionFailureWithUnauthzResponse() throws Exception {
            val request = new MockHttpServletRequest();
            val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId()));
            request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            var response = new MockHttpServletResponse();
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            val context = new MockRequestContext();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            val client = builtClients.findClient("FacebookClient").get();
            val webContext = new JEEContext(request, response);
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            request.addParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

            val event = delegatedAuthenticationAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_STOP, event.getId());
        }

        @Test
        public void verifyFinishAuthenticationAuthzFailure() throws Exception {
            val request = new MockHttpServletRequest();
            request.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FacebookClient");
            val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
            request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());


            val context = new MockRequestContext();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            val client = builtClients.findClient("FacebookClient").get();
            val webContext = new JEEContext(request, new MockHttpServletResponse());
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            request.addParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

            assertThrows(UnauthorizedServiceException.class,
                () -> delegatedAuthenticationAction.execute(context));
        }

        @Test
        public void verifySaml2LogoutResponse() throws Exception {
            val client = builtClients.findClient("SAML2Client").get();

            val request = new MockHttpServletRequest();
            request.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, client.getName());
            val webContext = new JEEContext(request, new MockHttpServletResponse());
            request.setMethod("POST");

            val logoutResponse = getLogoutResponse();
            request.setContent(EncodingUtils.encodeBase64(logoutResponse).getBytes(StandardCharsets.UTF_8));

            val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));
            request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            val context = new MockRequestContext();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            request.addParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());
            request.addParameter(Pac4jConstants.LOGOUT_ENDPOINT_PARAMETER, "https://httpbin.org/post");


            val event = delegatedAuthenticationAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_GENERATE, event.getId());
        }

        @Test
        public void verifyFinishAuthentication() throws Exception {
            val request = new MockHttpServletRequest();
            request.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FacebookClient");

            request.addParameter(ThemeChangeInterceptor.DEFAULT_PARAM_NAME, "theme");
            request.addParameter(LocaleChangeInterceptor.DEFAULT_PARAM_NAME, Locale.getDefault().getCountry());
            request.addParameter(CasProtocolConstants.PARAMETER_METHOD, HttpMethod.POST.name());
            val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));
            request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            val context = new MockRequestContext();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            val client = builtClients.findClient("FacebookClient").get();
            val webContext = new JEEContext(request, new MockHttpServletResponse());
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            request.addParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

            val event = delegatedAuthenticationAction.execute(context);

            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
            assertEquals("theme", request.getAttribute(ThemeChangeInterceptor.DEFAULT_PARAM_NAME));
            assertEquals(Locale.getDefault().getCountry(), request.getAttribute(LocaleChangeInterceptor.DEFAULT_PARAM_NAME));
            assertEquals(HttpMethod.POST.name(), request.getAttribute(CasProtocolConstants.PARAMETER_METHOD));
            assertEquals(service.getId(), request.getAttribute(CasProtocolConstants.PARAMETER_SERVICE));
            val flowScope = context.getFlowScope();
            assertEquals(service.getId(), ((Service) flowScope.get(CasProtocolConstants.PARAMETER_SERVICE)).getId());
            val credential = flowScope.get(CasWebflowConstants.VAR_ID_CREDENTIAL, ClientCredential.class);
            assertNotNull(credential);
            assertEquals("casuser", credential.getId());
        }

        @Test
        public void verifyFailedAuthentication() throws Exception {
            val mockRequest = new MockHttpServletRequest();
            mockRequest.setParameter("error_message", "bad authn");
            mockRequest.setParameter("error_code", "403");
            mockRequest.setParameter("error_description", "authentication failed");
            val service = CoreAuthenticationTestUtils.getService();
            mockRequest.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            val servletExternalContext = mock(ServletExternalContext.class);
            when(servletExternalContext.getNativeRequest()).thenReturn(mockRequest);
            when(servletExternalContext.getNativeResponse()).thenReturn(new MockHttpServletResponse());

            val mockRequestContext = new MockRequestContext();
            mockRequestContext.setExternalContext(servletExternalContext);

            assertEquals(CasWebflowConstants.TRANSITION_ID_STOP, delegatedAuthenticationAction.execute(mockRequestContext).getId());
        }

        @Test
        public void verifySsoAuthenticationWithUnauthorizedSso() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();

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
            request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            request.addParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

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
        public void verifySsoAuthentication() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();

            val client = builtClients.findClient("FacebookClient").orElse(null);
            val webContext = new JEEContext(request, new MockHttpServletResponse());
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            request.addParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

            request.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FacebookClient");
            val service = CoreAuthenticationTestUtils.getService("https://delegated2.example.org");
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));
            request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            val tgt = new MockTicketGrantingTicket("casuser");
            ticketRegistry.addTicket(tgt);
            WebUtils.putTicketGrantingTicketInScopes(context, tgt);
            assertEquals(CasWebflowConstants.TRANSITION_ID_GENERATE_SERVICE_TICKET, delegatedAuthenticationAction.execute(context).getId());
        }

        @Test
        public void verifySsoAuthenticationWithInvalidTicketFails() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            request.addParameter("error_message", "Auth+failed");
            val response = new MockHttpServletResponse();

            val client = builtClients.findClient("FacebookClient").get();
            val webContext = new JEEContext(request, new MockHttpServletResponse());
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            request.addParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

            request.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FacebookClient");
            val service = CoreAuthenticationTestUtils.getService("https://delegated2.example.org");
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));
            request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            val tgt = new MockTicketGrantingTicket("casuser");
            ticketRegistry.addTicket(tgt);
            WebUtils.putTicketGrantingTicketInScopes(context, new MockTicketGrantingTicket("otheruser"));
            assertEquals(CasWebflowConstants.TRANSITION_ID_STOP, delegatedAuthenticationAction.execute(context).getId());
        }

        @Test
        public void verifyLogoutRequestWithOkAction() throws Exception {
            val request = new MockHttpServletRequest();
            request.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "MockClientNoCredentials");
            request.addParameter(Pac4jConstants.LOGOUT_ENDPOINT_PARAMETER, "true");
            val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));
            request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

            val context = new MockRequestContext();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            val event = delegatedAuthenticationAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_GENERATE, event.getId());
        }

        @Test
        public void verifyServerSideRedirectAuthentication() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();

            val service = CoreAuthenticationTestUtils.getService("https://delegated2.example.org");
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));
            request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

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
        public void verifySsoAuthenticationUnauthz() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();

            request.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FacebookClient");
            val service = CoreAuthenticationTestUtils.getService("https://delegated3.example.org");
            request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
            val client = builtClients.findClient("FacebookClient").get();
            val webContext = new JEEContext(request, new MockHttpServletResponse());
            val ticket = delegatedClientAuthenticationWebflowManager.store(context, webContext, client);
            request.addParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

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
