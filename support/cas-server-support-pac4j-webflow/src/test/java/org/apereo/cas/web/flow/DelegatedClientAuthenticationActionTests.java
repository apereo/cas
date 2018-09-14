package org.apereo.cas.web.flow;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.AuthenticationTransactionManager;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.factory.DefaultTransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.DefaultTicketRegistry;
import org.apereo.cas.ticket.support.HardTimeoutExpirationPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.Pac4jUtils;
import org.apereo.cas.web.DelegatedClientNavigationController;
import org.apereo.cas.web.DelegatedClientWebflowManager;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.pac4j.DelegatedSessionCookieManager;
import org.apereo.cas.web.pac4j.SessionStoreCookieSerializer;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.junit.Test;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.oauth.credentials.OAuth20Credentials;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This class tests the {@link DelegatedClientAuthenticationAction} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
@Slf4j
public class DelegatedClientAuthenticationActionTests {

    private static final String TGT_ID = "TGT-00-xxxxxxxxxxxxxxxxxxxxxxxxxx.cas0";

    private static final String MY_KEY = "my_key";

    private static final String MY_SECRET = "my_secret";

    private static final String MY_LOGIN_URL = "http://casserver/login";

    private static final String MY_SERVICE = "http://myservice";

    private static final String MY_THEME = "my_theme";

    @Test
    public void verifyStartAuthenticationNoService() throws Exception {
        verifyStartAuthentication(null);
    }

    @Test
    public void verifyStartAuthenticationWithService() throws Exception {
        final Service service = RegisteredServiceTestUtils.getService(MY_SERVICE);
        verifyStartAuthentication(service);
    }

    private void verifyStartAuthentication(final Service service) throws Exception {
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        final String locale = Locale.getDefault().getCountry();
        mockRequest.setParameter(ThemeChangeInterceptor.DEFAULT_PARAM_NAME, MY_THEME);
        mockRequest.setParameter(LocaleChangeInterceptor.DEFAULT_PARAM_NAME, locale);
        mockRequest.setParameter(CasProtocolConstants.PARAMETER_METHOD, HttpMethod.POST.name());

        final ServletExternalContext servletExternalContext = mock(ServletExternalContext.class);
        when(servletExternalContext.getNativeRequest()).thenReturn(mockRequest);
        when(servletExternalContext.getNativeResponse()).thenReturn(mockResponse);

        final MockRequestContext mockRequestContext = new MockRequestContext();
        mockRequestContext.setExternalContext(servletExternalContext);

        if (service != null) {
            mockRequestContext.getFlowScope().put(CasProtocolConstants.PARAMETER_SERVICE, service);
        }

        final FacebookClient facebookClient = new FacebookClient(MY_KEY, MY_SECRET);
        final TwitterClient twitterClient = new TwitterClient("3nJPbVTVRZWAyUgoUKQ8UA", "h6LZyZJmcW46Vu8R47MYfeXTSYGI30EqnWaSwVhFkbA");
        final Clients clients = new Clients(MY_LOGIN_URL, facebookClient, twitterClient);
        final AuditableExecution enforcer = mock(AuditableExecution.class);
        when(enforcer.execute(any())).thenReturn(new AuditableExecutionResult());

        final DefaultTicketRegistry ticketRegistry = new DefaultTicketRegistry();
        final DelegatedClientWebflowManager manager = new DelegatedClientWebflowManager(ticketRegistry,
            new DefaultTransientSessionTicketFactory(new HardTimeoutExpirationPolicy(60)),
            ThemeChangeInterceptor.DEFAULT_PARAM_NAME, LocaleChangeInterceptor.DEFAULT_PARAM_NAME,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()),
            new DefaultArgumentExtractor(new WebApplicationServiceFactory()));
        final Ticket ticket = manager.store(Pac4jUtils.getPac4jJ2EContext(mockRequest, new MockHttpServletResponse()), facebookClient);

        mockRequest.addParameter(DelegatedClientWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

        final Event event = getDelegatedClientAction(facebookClient, service, clients, mockRequest).execute(mockRequestContext);
        assertEquals("error", event.getId());

        manager.retrieve(mockRequestContext, Pac4jUtils.getPac4jJ2EContext(mockRequest, new MockHttpServletResponse()), facebookClient);

        assertEquals(MY_THEME, mockRequest.getAttribute(ThemeChangeInterceptor.DEFAULT_PARAM_NAME));
        assertEquals(Locale.getDefault().getCountry(), mockRequest.getAttribute(LocaleChangeInterceptor.DEFAULT_PARAM_NAME));
        assertEquals(HttpMethod.POST.name(), mockRequest.getAttribute(CasProtocolConstants.PARAMETER_METHOD));
        final MutableAttributeMap flowScope = mockRequestContext.getFlowScope();
        final Set<DelegatedClientAuthenticationAction.ProviderLoginPageConfiguration> urls =
            (Set<DelegatedClientAuthenticationAction.ProviderLoginPageConfiguration>)
                flowScope.get(DelegatedClientAuthenticationAction.PAC4J_URLS);

        assertFalse(urls.isEmpty());
        assertSame(2, urls.size());
        urls.stream()
            .map(url -> UriComponentsBuilder.fromUriString(url.getRedirectUrl()).build())
            .forEach(uriComponents -> {
                assertThat(uriComponents.getPath()).isEqualTo(DelegatedClientNavigationController.ENDPOINT_REDIRECT);
                assertThat(uriComponents.getQueryParams().get(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER)).hasSize(1).isSubsetOf("FacebookClient", "TwitterClient");
                if (service != null) {
                    assertThat(uriComponents.getQueryParams().get(CasProtocolConstants.PARAMETER_SERVICE)).hasSize(1).contains(MY_SERVICE);
                } else {
                    assertThat(uriComponents.getQueryParams().get(CasProtocolConstants.PARAMETER_SERVICE)).isNull();
                }
                assertThat(uriComponents.getQueryParams().get(CasProtocolConstants.PARAMETER_METHOD)).hasSize(1).contains(HttpMethod.POST.toString());
                assertThat(uriComponents.getQueryParams().get(ThemeChangeInterceptor.DEFAULT_PARAM_NAME)).hasSize(1).contains(MY_THEME);
                assertThat(uriComponents.getQueryParams().get(LocaleChangeInterceptor.DEFAULT_PARAM_NAME)).hasSize(1).contains(locale);
            });
    }

    @Test
    public void verifyFinishAuthentication() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FacebookClient");

        mockRequest.addParameter(ThemeChangeInterceptor.DEFAULT_PARAM_NAME, MY_THEME);
        mockRequest.addParameter(LocaleChangeInterceptor.DEFAULT_PARAM_NAME, Locale.getDefault().getCountry());
        mockRequest.addParameter(CasProtocolConstants.PARAMETER_METHOD, HttpMethod.POST.name());
        final Service service = CoreAuthenticationTestUtils.getService(MY_SERVICE);
        mockRequest.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

        final ServletExternalContext servletExternalContext = mock(ServletExternalContext.class);
        when(servletExternalContext.getNativeRequest()).thenReturn(mockRequest);
        when(servletExternalContext.getNativeResponse()).thenReturn(new MockHttpServletResponse());

        final MockRequestContext mockRequestContext = new MockRequestContext();
        mockRequestContext.setExternalContext(servletExternalContext);

        final FacebookClient facebookClient = new FacebookClient() {
            @Override
            protected OAuth20Credentials retrieveCredentials(final WebContext context) {
                return new OAuth20Credentials("fakeVerifier");
            }
        };
        facebookClient.setName(FacebookClient.class.getSimpleName());
        final Clients clients = new Clients(MY_LOGIN_URL, facebookClient);

        final Event event = getDelegatedClientAction(facebookClient, service, clients, mockRequest).execute(mockRequestContext);
        assertEquals("success", event.getId());
        assertEquals(MY_THEME, mockRequest.getAttribute(ThemeChangeInterceptor.DEFAULT_PARAM_NAME));
        assertEquals(Locale.getDefault().getCountry(), mockRequest.getAttribute(LocaleChangeInterceptor.DEFAULT_PARAM_NAME));
        assertEquals(HttpMethod.POST.name(), mockRequest.getAttribute(CasProtocolConstants.PARAMETER_METHOD));
        assertEquals(MY_SERVICE, mockRequest.getAttribute(CasProtocolConstants.PARAMETER_SERVICE));
        final MutableAttributeMap flowScope = mockRequestContext.getFlowScope();
        assertEquals(service.getId(), ((Service) flowScope.get(CasProtocolConstants.PARAMETER_SERVICE)).getId());
        final ClientCredential credential = (ClientCredential) flowScope.get(CasWebflowConstants.VAR_ID_CREDENTIAL);
        assertNotNull(credential);
        assertTrue(credential.getId().startsWith(ClientCredential.NOT_YET_AUTHENTICATED));
    }

    private ServicesManager getServicesManagerWith(final Service service, final BaseClient client) {
        final ServicesManager mgr = mock(ServicesManager.class);
        final AbstractRegisteredService regSvc = service != null ? RegisteredServiceTestUtils.getRegisteredService(service.getId()) : null;

        if (regSvc != null) {
            final DefaultRegisteredServiceAccessStrategy strategy = new DefaultRegisteredServiceAccessStrategy();
            strategy.setDelegatedAuthenticationPolicy(new DefaultRegisteredServiceDelegatedAuthenticationPolicy(CollectionUtils.wrapList(client.getName())));
            regSvc.setAccessStrategy(strategy);
        }
        when(mgr.findServiceBy(any(Service.class))).thenReturn(regSvc);

        return mgr;
    }

    private AbstractAction getDelegatedClientAction(final BaseClient client, final Service service, final Clients clients,
                                                    final MockHttpServletRequest mockRequest) {
        final TicketGrantingTicket tgt = new TicketGrantingTicketImpl(TGT_ID, mock(Authentication.class), mock(ExpirationPolicy.class));
        final CentralAuthenticationService casImpl = mock(CentralAuthenticationService.class);
        when(casImpl.createTicketGrantingTicket(any())).thenReturn(tgt);

        final AuthenticationTransactionManager transManager = mock(AuthenticationTransactionManager.class);
        final AuthenticationManager authNManager = mock(AuthenticationManager.class);
        when(authNManager.authenticate(any(AuthenticationTransaction.class))).thenReturn(CoreAuthenticationTestUtils.getAuthentication());

        when(transManager.getAuthenticationManager()).thenReturn(authNManager);
        when(transManager.handle(any(AuthenticationTransaction.class), any(AuthenticationResultBuilder.class))).thenReturn(transManager);

        final AuthenticationResult authnResult = mock(AuthenticationResult.class);
        when(authnResult.getAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication());
        when(authnResult.getService()).thenReturn(service);

        final AuthenticationSystemSupport support = mock(AuthenticationSystemSupport.class);
        when(support.getAuthenticationTransactionManager()).thenReturn(transManager);
        when(support.handleAndFinalizeSingleAuthenticationTransaction(any(), (Credential[]) any())).thenReturn(authnResult);

        final AuditableExecution enforcer = mock(AuditableExecution.class);
        when(enforcer.execute(any())).thenReturn(new AuditableExecutionResult());
        final DefaultTicketRegistry ticketRegistry = new DefaultTicketRegistry();
        final DelegatedClientWebflowManager manager = new DelegatedClientWebflowManager(ticketRegistry,
            new DefaultTransientSessionTicketFactory(new HardTimeoutExpirationPolicy(60)),
            ThemeChangeInterceptor.DEFAULT_PARAM_NAME, LocaleChangeInterceptor.DEFAULT_PARAM_NAME,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()),
            new DefaultArgumentExtractor(new WebApplicationServiceFactory()));
        final Ticket ticket = manager.store(Pac4jUtils.getPac4jJ2EContext(mockRequest, new MockHttpServletResponse()), client);

        mockRequest.addParameter(DelegatedClientWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());
        final CasDelegatingWebflowEventResolver initialResolver = mock(CasDelegatingWebflowEventResolver.class);
        when(initialResolver.resolveSingle(any())).thenReturn(new Event(this, "success"));

        return new DelegatedClientAuthenticationAction(
            initialResolver,
            mock(CasWebflowEventResolver.class),
            mock(AdaptiveAuthenticationPolicy.class),
            clients,
            getServicesManagerWith(service, client),
            enforcer,
            manager,
            new DelegatedSessionCookieManager(mock(CookieRetrievingCookieGenerator.class), mock(SessionStoreCookieSerializer.class)),
            support,
            LocaleChangeInterceptor.DEFAULT_PARAM_NAME,
            ThemeChangeInterceptor.DEFAULT_PARAM_NAME,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));

    }
}
