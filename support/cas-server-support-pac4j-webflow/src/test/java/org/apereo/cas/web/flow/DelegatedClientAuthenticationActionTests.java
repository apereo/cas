package org.apereo.cas.web.flow;

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
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.RegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.builder.TransientSessionTicketExpirationPolicyBuilder;
import org.apereo.cas.ticket.factory.DefaultTransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.DefaultTicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfigurationFactory;
import org.apereo.cas.web.DelegatedClientWebflowManager;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.DefaultArgumentExtractor;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.oauth.credentials.OAuth20Credentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.FlowVariable;
import org.springframework.webflow.engine.support.BeanFactoryVariableValueFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockFlowExecutionContext;
import org.springframework.webflow.test.MockFlowSession;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This class tests the {@link DelegatedClientAuthenticationAction} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
public class DelegatedClientAuthenticationActionTests {

    private static final String TGT_ID = "TGT-00-xxxxxxxxxxxxxxxxxxxxxxxxxx.cas0";

    private static final String MY_KEY = "my_key";

    private static final String MY_SECRET = "my_secret";

    private static final String MY_LOGIN_URL = "http://casserver/login";

    private static final String MY_SERVICE = "http://myservice";

    private static final String MY_THEME = "my_theme";
    private static final List<String> CLIENTS = Arrays.asList("FacebookClient", "TwitterClient");

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyStartAuthenticationNoService() {
        assertStartAuthentication(null);
    }

    @Test
    public void verifyStartAuthenticationWithService() {
        val service = RegisteredServiceTestUtils.getService(MY_SERVICE);
        assertStartAuthentication(service);
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    private void assertStartAuthentication(final Service service) {
        val mockResponse = new MockHttpServletResponse();
        val mockRequest = new MockHttpServletRequest();
        val locale = Locale.getDefault().getCountry();
        mockRequest.setParameter(ThemeChangeInterceptor.DEFAULT_PARAM_NAME, MY_THEME);
        mockRequest.setParameter(LocaleChangeInterceptor.DEFAULT_PARAM_NAME, locale);
        mockRequest.setParameter(CasProtocolConstants.PARAMETER_METHOD, HttpMethod.POST.name());

        val servletExternalContext = mock(ServletExternalContext.class);
        when(servletExternalContext.getNativeRequest()).thenReturn(mockRequest);
        when(servletExternalContext.getNativeResponse()).thenReturn(mockResponse);

        val flow = new Flow("mockFlow");
        flow.addVariable(new FlowVariable("credential",
            new BeanFactoryVariableValueFactory(UsernamePasswordCredential.class, applicationContext.getAutowireCapableBeanFactory())));
        val mockRequestContext = new MockRequestContext();
        val mockExecutionContext = new MockFlowExecutionContext(new MockFlowSession(flow));
        mockRequestContext.setFlowExecutionContext(mockExecutionContext);
        mockRequestContext.setExternalContext(servletExternalContext);

        if (service != null) {
            mockRequestContext.getFlowScope().put(CasProtocolConstants.PARAMETER_SERVICE, service);
        }

        val facebookClient = new FacebookClient(MY_KEY, MY_SECRET);
        val twitterClient = new TwitterClient("3nJPbVTVRZWAyUgoUKQ8UA", "h6LZyZJmcW46Vu8R47MYfeXTSYGI30EqnWaSwVhFkbA");
        val clients = new Clients(MY_LOGIN_URL, facebookClient, twitterClient);
        val enforcer = mock(AuditableExecution.class);
        when(enforcer.execute(any())).thenReturn(new AuditableExecutionResult());

        val ticketRegistry = new DefaultTicketRegistry();
        val manager = new DelegatedClientWebflowManager(ticketRegistry,
            new DefaultTransientSessionTicketFactory(getExpirationPolicyBuilder()),
            new CasConfigurationProperties(),
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()),
            new DefaultArgumentExtractor(new WebApplicationServiceFactory()));

        val webContext = new JEEContext(mockRequest, new MockHttpServletResponse(), new JEESessionStore());
        val ticket = manager.store(webContext, facebookClient);

        mockRequest.addParameter(DelegatedClientWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());

        val strategy = new DefaultRegisteredServiceAccessStrategy();
        strategy.setDelegatedAuthenticationPolicy(
            new DefaultRegisteredServiceDelegatedAuthenticationPolicy(
                CollectionUtils.wrapList(facebookClient.getName()), true, false));

        val event = getDelegatedClientAction(facebookClient, service, clients, mockRequest, strategy).execute(mockRequestContext);
        assertEquals("error", event.getId());

        manager.retrieve(mockRequestContext, webContext, facebookClient);

        assertEquals(MY_THEME, mockRequest.getAttribute(ThemeChangeInterceptor.DEFAULT_PARAM_NAME));
        assertEquals(Locale.getDefault().getCountry(), mockRequest.getAttribute(LocaleChangeInterceptor.DEFAULT_PARAM_NAME));
        assertEquals(HttpMethod.POST.name(), mockRequest.getAttribute(CasProtocolConstants.PARAMETER_METHOD));
        val flowScope = mockRequestContext.getFlowScope();
        val urls = (Set<DelegatedClientIdentityProviderConfiguration>)
            flowScope.get(DelegatedClientAuthenticationAction.FLOW_ATTRIBUTE_PROVIDER_URLS);

        assertFalse(urls.isEmpty());
        assertSame(2, urls.size());
        urls.stream()
            .map(url -> UriComponentsBuilder.fromUriString(url.getRedirectUrl()).build())
            .forEach(uriComponents -> {
                assertEquals(DelegatedClientIdentityProviderConfigurationFactory.ENDPOINT_URL_REDIRECT, uriComponents.getPath());
                val clientName = uriComponents.getQueryParams().get(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER);
                assertEquals(1, clientName.size());
                assertTrue(CLIENTS.containsAll(clientName));
                val serviceName = uriComponents.getQueryParams().get(CasProtocolConstants.PARAMETER_SERVICE);
                if (service != null) {
                    assertEquals(1, serviceName.size());
                    assertTrue(serviceName.contains(MY_SERVICE));
                } else {
                    assertNull(serviceName);
                }
                val method = uriComponents.getQueryParams().get(CasProtocolConstants.PARAMETER_METHOD);
                assertEquals(1, method.size());
                assertTrue(method.contains(HttpMethod.POST.toString()));
                val theme = uriComponents.getQueryParams().get(ThemeChangeInterceptor.DEFAULT_PARAM_NAME);
                assertEquals(1, theme.size());
                assertTrue(theme.contains(MY_THEME));
                val testLocale = uriComponents.getQueryParams().get(LocaleChangeInterceptor.DEFAULT_PARAM_NAME);
                assertEquals(1, testLocale.size());
                assertTrue(testLocale.contains(locale));
            });
    }

    private static TransientSessionTicketExpirationPolicyBuilder getExpirationPolicyBuilder() {
        val props = new CasConfigurationProperties();
        props.getTicket().getTst().setTimeToKillInSeconds(60);
        return new TransientSessionTicketExpirationPolicyBuilder(props);
    }

    @Test
    public void verifyFinishAuthenticationAuthzFailure() {
        val mockRequest = new MockHttpServletRequest();
        mockRequest.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FacebookClient");
        val service = CoreAuthenticationTestUtils.getService(MY_SERVICE);
        mockRequest.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

        val servletExternalContext = mock(ServletExternalContext.class);
        when(servletExternalContext.getNativeRequest()).thenReturn(mockRequest);
        when(servletExternalContext.getNativeResponse()).thenReturn(new MockHttpServletResponse());

        val mockRequestContext = new MockRequestContext();
        mockRequestContext.setExternalContext(servletExternalContext);

        val facebookClient = new FacebookClient() {
            @Override
            protected Optional<OAuth20Credentials> retrieveCredentials(final WebContext context) {
                return Optional.of(new OAuth20Credentials("fakeVerifier"));
            }
        };
        facebookClient.setName(FacebookClient.class.getSimpleName());
        val clients = new Clients(MY_LOGIN_URL, facebookClient);

        val strategy = new DefaultRegisteredServiceAccessStrategy();
        strategy.setEnabled(false);

        assertThrows(UnauthorizedServiceException.class,
            () -> getDelegatedClientAction(facebookClient, service, clients, mockRequest, strategy).execute(mockRequestContext));
    }

    @Test
    @SneakyThrows
    public void verifyFinishAuthentication() {
        val mockRequest = new MockHttpServletRequest();
        mockRequest.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "FacebookClient");

        mockRequest.addParameter(ThemeChangeInterceptor.DEFAULT_PARAM_NAME, MY_THEME);
        mockRequest.addParameter(LocaleChangeInterceptor.DEFAULT_PARAM_NAME, Locale.getDefault().getCountry());
        mockRequest.addParameter(CasProtocolConstants.PARAMETER_METHOD, HttpMethod.POST.name());
        val service = CoreAuthenticationTestUtils.getService(MY_SERVICE);
        mockRequest.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

        val servletExternalContext = mock(ServletExternalContext.class);
        when(servletExternalContext.getNativeRequest()).thenReturn(mockRequest);
        when(servletExternalContext.getNativeResponse()).thenReturn(new MockHttpServletResponse());

        val mockRequestContext = new MockRequestContext();
        mockRequestContext.setExternalContext(servletExternalContext);

        val facebookClient = new FacebookClient() {
            @Override
            protected Optional<OAuth20Credentials> retrieveCredentials(final WebContext context) {
                return Optional.of(new OAuth20Credentials("fakeVerifier"));
            }
        };
        facebookClient.setName(FacebookClient.class.getSimpleName());
        val clients = new Clients(MY_LOGIN_URL, facebookClient);

        val strategy = new DefaultRegisteredServiceAccessStrategy();
        strategy.setDelegatedAuthenticationPolicy(
            new DefaultRegisteredServiceDelegatedAuthenticationPolicy(
                CollectionUtils.wrapList(facebookClient.getName()), true, false));

        val event = getDelegatedClientAction(facebookClient, service, clients, mockRequest, strategy).execute(mockRequestContext);
        assertEquals("success", event.getId());
        assertEquals(MY_THEME, mockRequest.getAttribute(ThemeChangeInterceptor.DEFAULT_PARAM_NAME));
        assertEquals(Locale.getDefault().getCountry(), mockRequest.getAttribute(LocaleChangeInterceptor.DEFAULT_PARAM_NAME));
        assertEquals(HttpMethod.POST.name(), mockRequest.getAttribute(CasProtocolConstants.PARAMETER_METHOD));
        assertEquals(MY_SERVICE, mockRequest.getAttribute(CasProtocolConstants.PARAMETER_SERVICE));
        val flowScope = mockRequestContext.getFlowScope();
        assertEquals(service.getId(), ((Service) flowScope.get(CasProtocolConstants.PARAMETER_SERVICE)).getId());
        val credential = (ClientCredential) flowScope.get(CasWebflowConstants.VAR_ID_CREDENTIAL);
        assertNotNull(credential);
        assertTrue(credential.getId().startsWith(ClientCredential.NOT_YET_AUTHENTICATED));
    }

    private static ServicesManager getServicesManagerWith(final Service service, final RegisteredServiceAccessStrategy accessStrategy) {
        val mgr = mock(ServicesManager.class);
        val regSvc = Optional.ofNullable(service)
            .map(svc -> RegisteredServiceTestUtils.getRegisteredService(svc.getId()))
            .orElse(null);

        if (regSvc != null) {
            regSvc.setAccessStrategy(accessStrategy);
        }
        when(mgr.findServiceBy(any(Service.class))).thenReturn(regSvc);

        return mgr;
    }

    private AbstractAction getDelegatedClientAction(final BaseClient client,
                                                    final Service service,
                                                    final Clients clients,
                                                    final MockHttpServletRequest mockRequest,
                                                    final RegisteredServiceAccessStrategy accessStrategy) {
        val tgt = new TicketGrantingTicketImpl(TGT_ID, mock(Authentication.class), mock(ExpirationPolicy.class));
        val casImpl = mock(CentralAuthenticationService.class);
        when(casImpl.createTicketGrantingTicket(any())).thenReturn(tgt);

        val transManager = mock(AuthenticationTransactionManager.class);
        val authNManager = mock(AuthenticationManager.class);
        when(authNManager.authenticate(any(AuthenticationTransaction.class))).thenReturn(CoreAuthenticationTestUtils.getAuthentication());

        when(transManager.getAuthenticationManager()).thenReturn(authNManager);
        when(transManager.handle(any(AuthenticationTransaction.class), any(AuthenticationResultBuilder.class))).thenReturn(transManager);

        val authnResult = mock(AuthenticationResult.class);
        when(authnResult.getAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication());
        when(authnResult.getService()).thenReturn(service);

        val support = mock(AuthenticationSystemSupport.class);
        when(support.getAuthenticationTransactionManager()).thenReturn(transManager);
        when(support.handleAndFinalizeSingleAuthenticationTransaction(any(), (Credential[]) any())).thenReturn(authnResult);

        val enforcer = mock(AuditableExecution.class);
        when(enforcer.execute(any())).thenReturn(new AuditableExecutionResult());
        val ticketRegistry = new DefaultTicketRegistry();
        val manager = new DelegatedClientWebflowManager(ticketRegistry,
            new DefaultTransientSessionTicketFactory(getExpirationPolicyBuilder()),
            new CasConfigurationProperties(),
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()),
            new DefaultArgumentExtractor(new WebApplicationServiceFactory()));
        val webContext = new JEEContext(mockRequest, new MockHttpServletResponse(), new JEESessionStore());
        val ticket = manager.store(webContext, client);

        mockRequest.addParameter(DelegatedClientWebflowManager.PARAMETER_CLIENT_ID, ticket.getId());
        val initialResolver = mock(CasDelegatingWebflowEventResolver.class);
        when(initialResolver.resolveSingle(any())).thenReturn(new Event(this, "success"));

        return new DelegatedClientAuthenticationAction(
            initialResolver,
            mock(CasWebflowEventResolver.class),
            mock(AdaptiveAuthenticationPolicy.class),
            clients,
            getServicesManagerWith(service, accessStrategy),
            enforcer,
            manager,
            support,
            new CasConfigurationProperties(),
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()),
            mock(CentralAuthenticationService.class),
            SingleSignOnParticipationStrategy.alwaysParticipating(),
            new JEESessionStore(),
            CollectionUtils.wrap(new DefaultArgumentExtractor(new WebApplicationServiceFactory())));
    }
}
