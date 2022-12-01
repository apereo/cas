package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.MockServletContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfigurationFactory;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.client.Clients;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpMethod;
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

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseDelegatedClientAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
public abstract class BaseDelegatedClientAuthenticationActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION)
    protected Action delegatedAuthenticationAction;

    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    @Autowired
    @Qualifier(DelegatedClientAuthenticationWebflowManager.DEFAULT_BEAN_NAME)
    protected DelegatedClientAuthenticationWebflowManager delegatedClientAuthenticationWebflowManager;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    protected TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CREATE_CLIENTS)
    protected Action delegatedAuthenticationCreateClientsAction;

    @Autowired
    @Qualifier("builtClients")
    protected Clients builtClients;

    protected String getLogoutResponse() {
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

    protected void assertStartAuthentication(final Service service) throws Exception {
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

        val ticket = delegatedClientAuthenticationWebflowManager.store(requestContext, webContext, client);
        request.addParameter(DelegatedClientAuthenticationWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

        LOGGER.debug("Initializing action with request parameters [{}]", webContext.getRequestParameters());
        val event = delegatedAuthenticationAction.execute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_GENERATE, event.getId());

        val generated = delegatedAuthenticationCreateClientsAction.execute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, generated.getId());

        delegatedClientAuthenticationWebflowManager.retrieve(requestContext, webContext, client);

        assertEquals("theme", request.getAttribute(ThemeChangeInterceptor.DEFAULT_PARAM_NAME));
        assertEquals(locale, request.getAttribute(LocaleChangeInterceptor.DEFAULT_PARAM_NAME));
        assertEquals(HttpMethod.POST.name(), request.getAttribute(CasProtocolConstants.PARAMETER_METHOD));
        val urls = (Set<DelegatedClientIdentityProviderConfiguration>)
            DelegationWebflowUtils.getDelegatedAuthenticationProviderConfigurations(requestContext);

        assertFalse(urls.isEmpty());
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
                    assertEquals(1, serviceName.size(), () -> service.getId() + " must be exactly 1");
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
