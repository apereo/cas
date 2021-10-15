package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.AllAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceAuthenticationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.MockServletContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.DefaultDelegatedClientAuthenticationWebflowManager;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfigurationFactory;
import org.apereo.cas.web.support.WebUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.FlowVariable;
import org.springframework.webflow.engine.support.BeanFactoryVariableValueFactory;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockFlowExecutionContext;
import org.springframework.webflow.test.MockFlowSession;
import org.springframework.webflow.test.MockRequestContext;

import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.webflow.context.ExternalContextHolder.*;
import static org.springframework.webflow.execution.RequestContextHolder.*;

/**
 * This class tests the {@link DelegatedClientAuthenticationAction} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
@Tag("WebflowAuthenticationActions")
@Slf4j
public class DelegatedClientAuthenticationActionTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION)
    private Action delegatedAuthenticationAction;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier(DelegatedClientAuthenticationWebflowManager.DEFAULT_BEAN_NAME)
    private DelegatedClientAuthenticationWebflowManager delegatedClientAuthenticationWebflowManager;

    @Autowired
    @Qualifier(CentralAuthenticationService.BEAN_NAME)
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("builtClients")
    private Clients builtClients;

    private static String getLogoutResponse() {
        return "<samlp:LogoutResponse xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" "
            + "xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\" "
            + "ID=\"_6c3737282f007720e736f0f4028feed8cb9b40291c\" Version=\"2.0\" "
            + "IssueInstant=\"" + ZonedDateTime.now(ZoneOffset.UTC) + "\" "
            + "Destination=\"http://callback.example.org?client_name=SAML2Client&amp;logoutendpoint=true\" "
            + "InResponseTo=\"ONELOGIN_21df91a89767879fc0f7df6a1490c6000c81644d\">%n"
            + "  <saml:Issuer>https://cas.example.org/idp</saml:Issuer>%n"
            + "  <samlp:Status>%n"
            + "    <samlp:StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:Success\"/>%n"
            + "  </samlp:Status>%n"
            + "</samlp:LogoutResponse>";
    }

    @Test
    public void verifyStartAuthenticationNoService() {
        assertStartAuthentication(null);
    }

    @Test
    public void verifyStartAuthenticationWithService() {
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

        val client = builtClients.findClient("FacebookClient").get();
        var response = new MockHttpServletResponse();
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        val webContext = new JEEContext(request, response);
        val ticket = delegatedClientAuthenticationWebflowManager.store(webContext, client);
        request.addParameter(DefaultDelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        setRequestContext(context);
        setExternalContext(context.getExternalContext());

        val event = delegatedAuthenticationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_STOP, event.getId());
    }

    @Test
    public void verifyFinishAuthenticationAuthzFailure() {
        val request = new MockHttpServletRequest();
        request.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FacebookClient");
        val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

        val client = builtClients.findClient("FacebookClient").get();
        val webContext = new JEEContext(request, new MockHttpServletResponse());
        val ticket = delegatedClientAuthenticationWebflowManager.store(webContext, client);
        request.addParameter(DefaultDelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

        val context = new MockRequestContext();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        setRequestContext(context);
        setExternalContext(context.getExternalContext());

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

        val ticket = delegatedClientAuthenticationWebflowManager.store(webContext, client);
        request.addParameter(DefaultDelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());
        request.addParameter(Pac4jConstants.LOGOUT_ENDPOINT_PARAMETER, "https://httpbin.org/post");

        val context = new MockRequestContext();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        setRequestContext(context);
        setExternalContext(context.getExternalContext());

        val event = delegatedAuthenticationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
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

        val client = builtClients.findClient("FacebookClient").get();
        val webContext = new JEEContext(request, new MockHttpServletResponse());
        val ticket = delegatedClientAuthenticationWebflowManager.store(webContext, client);
        request.addParameter(DefaultDelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

        val context = new MockRequestContext();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        setRequestContext(context);
        setExternalContext(context.getExternalContext());

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
        assertEquals(credential.getId(), "casuser");
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

        val client = builtClients.findClient("FacebookClient").get();
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
        val ticket = delegatedClientAuthenticationWebflowManager.store(webContext, client);
        request.addParameter(DefaultDelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());
        
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        setRequestContext(context);
        setExternalContext(context.getExternalContext());

        val tgt = new MockTicketGrantingTicket("casuser");
        centralAuthenticationService.addTicket(tgt);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, delegatedAuthenticationAction.execute(context).getId());
        assertThrows(InvalidTicketException.class, () -> centralAuthenticationService.getTicket(tgt.getId()));
    }
    
    @Test
    public void verifySsoAuthentication() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val client = builtClients.findClient("FacebookClient").get();
        val webContext = new JEEContext(request, new MockHttpServletResponse());
        val ticket = delegatedClientAuthenticationWebflowManager.store(webContext, client);
        request.addParameter(DefaultDelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

        request.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FacebookClient");
        val service = CoreAuthenticationTestUtils.getService("https://delegated2.example.org");
        servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        setRequestContext(context);
        setExternalContext(context.getExternalContext());

        val tgt = new MockTicketGrantingTicket("casuser");
        centralAuthenticationService.addTicket(tgt);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, delegatedAuthenticationAction.execute(context).getId());
    }

    @Test
    public void verifySsoAuthenticationWithInvalidTicketFails() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.addParameter("error_message", "Auth+failed");
        val response = new MockHttpServletResponse();

        val client = builtClients.findClient("FacebookClient").get();
        val webContext = new JEEContext(request, new MockHttpServletResponse());
        val ticket = delegatedClientAuthenticationWebflowManager.store(webContext, client);
        request.addParameter(DefaultDelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

        request.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FacebookClient");
        val service = CoreAuthenticationTestUtils.getService("https://delegated2.example.org");
        servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        setRequestContext(context);
        setExternalContext(context.getExternalContext());

        val tgt = new MockTicketGrantingTicket("casuser");
        centralAuthenticationService.addTicket(tgt);
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
        setRequestContext(context);
        setExternalContext(context.getExternalContext());

        val event = delegatedAuthenticationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
    }
    
    @Test
    public void verifySsoAuthenticationUnauthz() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        request.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FacebookClient");
        val service = CoreAuthenticationTestUtils.getService("https://delegated3.example.org");
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        val client = builtClients.findClient("FacebookClient").get();
        val webContext = new JEEContext(request, new MockHttpServletResponse());
        val ticket = delegatedClientAuthenticationWebflowManager.store(webContext, client);
        request.addParameter(DefaultDelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

        val accessStrategy = new DefaultRegisteredServiceAccessStrategy();
        accessStrategy.setEnabled(false);
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());
        registeredService.setAccessStrategy(accessStrategy);
        servicesManager.save(registeredService);

        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        setRequestContext(context);
        setExternalContext(context.getExternalContext());

        val tgt = new MockTicketGrantingTicket("casuser", Map.of(),
            Map.of(ClientCredential.AUTHENTICATION_ATTRIBUTE_CLIENT_NAME, List.of("FacebookClient")));
        centralAuthenticationService.addTicket(tgt);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);

        assertThrows(UnauthorizedServiceException.class, () -> delegatedAuthenticationAction.execute(context).getId());
        assertThrows(InvalidTicketException.class, () -> centralAuthenticationService.getTicket(tgt.getId()));
    }

    @SneakyThrows
    private void assertStartAuthentication(final Service service) {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val flow = new Flow("mockFlow");
        flow.addVariable(new FlowVariable("credential",
            new BeanFactoryVariableValueFactory(UsernamePasswordCredential.class, applicationContext.getAutowireCapableBeanFactory())));
        val locale = Locale.ENGLISH.getLanguage();
        request.setParameter(ThemeChangeInterceptor.DEFAULT_PARAM_NAME, "theme");
        LOGGER.debug("Setting locale [{}] for request parameter as [{}]", locale, request.getParameterMap());
        request.setParameter(LocaleChangeInterceptor.DEFAULT_PARAM_NAME, locale);
        request.setParameter(CasProtocolConstants.PARAMETER_METHOD, HttpMethod.POST.name());
        LOGGER.debug("Set request parameters as [{}]", request.getParameterMap());
        val requestContext = new MockRequestContext();
        requestContext.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(requestContext);
        ExternalContextHolder.setExternalContext(requestContext.getExternalContext());
        val mockExecutionContext = new MockFlowExecutionContext(new MockFlowSession(flow));
        requestContext.setFlowExecutionContext(mockExecutionContext);
        if (service != null) {
            WebUtils.putServiceIntoFlowScope(requestContext, service);
        }

        val client = builtClients.findClient("SAML2Client").get();
        val webContext = new JEEContext(request, response);

        val ticket = delegatedClientAuthenticationWebflowManager.store(webContext, client);
        request.addParameter(DefaultDelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

        LOGGER.debug("Initializing action with request parameters [{}]", webContext.getRequestParameters());
        val event = delegatedAuthenticationAction.execute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());

        delegatedClientAuthenticationWebflowManager.retrieve(requestContext, webContext, client);

        assertEquals("theme", request.getAttribute(ThemeChangeInterceptor.DEFAULT_PARAM_NAME));
        assertEquals(locale, request.getAttribute(LocaleChangeInterceptor.DEFAULT_PARAM_NAME));
        assertEquals(HttpMethod.POST.name(), request.getAttribute(CasProtocolConstants.PARAMETER_METHOD));
        val urls = (Set<DelegatedClientIdentityProviderConfiguration>)
            WebUtils.getDelegatedAuthenticationProviderConfigurations(requestContext);

        assertFalse(urls.isEmpty());
        assertSame(4, urls.size());
        urls.stream()
            .map(url -> {
                LOGGER.debug("Redirect URL [{}]", url.getRedirectUrl());
                return UriComponentsBuilder.fromUriString(url.getRedirectUrl()).build();
            })
            .forEach(uriComponents -> {
                assertEquals(DelegatedClientIdentityProviderConfigurationFactory.ENDPOINT_URL_REDIRECT, uriComponents.getPath());
                val clientName = uriComponents.getQueryParams().get(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER);
                assertEquals(1, clientName.size());

                val serviceName = uriComponents.getQueryParams().get(CasProtocolConstants.PARAMETER_SERVICE);
                if (service != null) {
                    assertEquals(1, serviceName.size());
                    assertTrue(serviceName.contains(EncodingUtils.urlEncode(RegisteredServiceTestUtils.CONST_TEST_URL)));
                } else {
                    assertNull(serviceName);
                }
                val method = uriComponents.getQueryParams().get(CasProtocolConstants.PARAMETER_METHOD);
                assertEquals(1, method.size());
                assertTrue(method.contains(HttpMethod.POST.toString()));
                val theme = uriComponents.getQueryParams().get(ThemeChangeInterceptor.DEFAULT_PARAM_NAME);
                assertEquals(1, theme.size());
                assertTrue(theme.contains("theme"));
                val testLocale = uriComponents.getQueryParams().get(LocaleChangeInterceptor.DEFAULT_PARAM_NAME);
                assertEquals(1, testLocale.size());
                assertTrue(testLocale.contains(locale));
            });
    }
}
