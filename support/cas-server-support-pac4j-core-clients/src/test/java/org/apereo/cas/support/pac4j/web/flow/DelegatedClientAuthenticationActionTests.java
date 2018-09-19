package org.apereo.cas.support.pac4j.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.junit.Test;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.WebContext;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.credentials.OAuth20Credentials;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This class tests the {@link DelegatedClientAuthenticationAction} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
public class DelegatedClientAuthenticationActionTests {

    private static final String TGT_ID = "TGT-00-xxxxxxxxxxxxxxxxxxxxxxxxxx.cas0";

    private static final String MY_KEY = "my_key";

    private static final String MY_SECRET = "my_secret";

    private static final String PAC4J_BASE_URL = "http://www.pac4j.org/";

    private static final String PAC4J_URL = PAC4J_BASE_URL + "test.html";

    private static final String MY_SERVICE = "http://myservice";

    private static final String MY_THEME = "my_theme";

    private static final String MY_LOCALE = "fr";

    @Test
    public void verifyStartAuthentication() throws Exception {
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setParameter(ThemeChangeInterceptor.DEFAULT_PARAM_NAME, MY_THEME);
        mockRequest.setParameter(LocaleChangeInterceptor.DEFAULT_PARAM_NAME, MY_LOCALE);
        mockRequest.setParameter(CasProtocolConstants.PARAMETER_METHOD, HttpMethod.POST.name());

        final MockHttpSession mockSession = new MockHttpSession();
        mockRequest.setSession(mockSession);

        final ServletExternalContext servletExternalContext = mock(ServletExternalContext.class);
        when(servletExternalContext.getNativeRequest()).thenReturn(mockRequest);
        when(servletExternalContext.getNativeResponse()).thenReturn(mockResponse);

        final MockRequestContext mockRequestContext = new MockRequestContext();
        mockRequestContext.setExternalContext(servletExternalContext);
        mockRequestContext.getFlowScope().put(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.getService(MY_SERVICE));

        final FacebookClient facebookClient = new FacebookClient(MY_KEY, MY_SECRET);
        final Clients clients = new Clients(PAC4J_URL, facebookClient);

        final CasDelegatingWebflowEventResolver initialResolver = mock(CasDelegatingWebflowEventResolver.class);
        when(initialResolver.resolveSingle(any())).thenReturn(new Event(this, "success"));

        final DelegatedClientAuthenticationAction action = new DelegatedClientAuthenticationAction(
            initialResolver,
            mock(CasWebflowEventResolver.class),
            mock(AdaptiveAuthenticationPolicy.class),
            clients,
            null, mock(CentralAuthenticationService.class),
            ThemeChangeInterceptor.DEFAULT_PARAM_NAME, LocaleChangeInterceptor.DEFAULT_PARAM_NAME, false);

        final Event event = action.execute(mockRequestContext);
        assertEquals("error", event.getId());
        assertEquals(MY_THEME, mockSession.getAttribute(ThemeChangeInterceptor.DEFAULT_PARAM_NAME));
        assertEquals(MY_LOCALE, mockSession.getAttribute(LocaleChangeInterceptor.DEFAULT_PARAM_NAME));
        assertEquals(HttpMethod.POST.name(), mockSession.getAttribute(CasProtocolConstants.PARAMETER_METHOD));
        final MutableAttributeMap flowScope = mockRequestContext.getFlowScope();
        final Set<DelegatedClientAuthenticationAction.ProviderLoginPageConfiguration> urls =
            (Set<DelegatedClientAuthenticationAction.ProviderLoginPageConfiguration>)
                flowScope.get(DelegatedClientAuthenticationAction.PAC4J_URLS);

        assertFalse(urls.isEmpty());
        assertEquals(1, urls.size());
    }

    @Test
    public void verifyFinishAuthentication() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setParameter(Clients.DEFAULT_CLIENT_NAME_PARAMETER, "FacebookClient");

        final MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute(ThemeChangeInterceptor.DEFAULT_PARAM_NAME, MY_THEME);
        mockSession.setAttribute(LocaleChangeInterceptor.DEFAULT_PARAM_NAME, MY_LOCALE);
        mockSession.setAttribute(CasProtocolConstants.PARAMETER_METHOD, HttpMethod.POST.name());
        final Service service = CoreAuthenticationTestUtils.getService(MY_SERVICE);
        mockSession.setAttribute(CasProtocolConstants.PARAMETER_SERVICE, service);
        mockRequest.setSession(mockSession);

        final ServletExternalContext servletExternalContext = mock(ServletExternalContext.class);
        when(servletExternalContext.getNativeRequest()).thenReturn(mockRequest);
        when(servletExternalContext.getNativeResponse()).thenReturn(new MockHttpServletResponse());

        final MockRequestContext mockRequestContext = new MockRequestContext();
        mockRequestContext.setExternalContext(servletExternalContext);

        final FacebookClient facebookClient = new FacebookClient() {
            @Override
            protected OAuth20Credentials retrieveCredentials(final WebContext context) {
                return new OAuth20Credentials("fakeVerifier", FacebookClient.class.getSimpleName());
            }
        };
        facebookClient.setName(FacebookClient.class.getSimpleName());
        final Clients clients = new Clients(PAC4J_URL, facebookClient);

        final CentralAuthenticationService casImpl = mock(CentralAuthenticationService.class);

        final AuthenticationSystemSupport support = mock(AuthenticationSystemSupport.class);

        final CasDelegatingWebflowEventResolver initialResolver = mock(CasDelegatingWebflowEventResolver.class);
        when(initialResolver.resolveSingle(any())).thenReturn(new Event(this, "success"));

        final DelegatedClientAuthenticationAction action = new DelegatedClientAuthenticationAction(
            initialResolver,
            mock(CasWebflowEventResolver.class),
            mock(AdaptiveAuthenticationPolicy.class),
            clients, support, casImpl,
            "theme", "locale", false);

        final Event event = action.execute(mockRequestContext);
        assertEquals("success", event.getId());
        assertEquals(MY_THEME, mockRequest.getAttribute(ThemeChangeInterceptor.DEFAULT_PARAM_NAME));
        assertEquals(MY_LOCALE, mockRequest.getAttribute(LocaleChangeInterceptor.DEFAULT_PARAM_NAME));
        assertEquals(HttpMethod.POST.name(), mockRequest.getAttribute(CasProtocolConstants.PARAMETER_METHOD));
        assertEquals(MY_SERVICE, mockRequest.getAttribute(CasProtocolConstants.PARAMETER_SERVICE));
        final MutableAttributeMap flowScope = mockRequestContext.getFlowScope();
        assertEquals(service, flowScope.get(CasProtocolConstants.PARAMETER_SERVICE));
        final ClientCredential credential = (ClientCredential) flowScope.get(CasWebflowConstants.VAR_ID_CREDENTIAL);
        assertNotNull(credential);
        assertTrue(credential.getId().startsWith(ClientCredential.NOT_YET_AUTHENTICATED));
    }
}
