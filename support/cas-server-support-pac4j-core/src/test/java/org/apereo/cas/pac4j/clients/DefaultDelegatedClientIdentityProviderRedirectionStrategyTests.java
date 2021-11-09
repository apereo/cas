package org.apereo.cas.pac4j.clients;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pac4j.client.DefaultDelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.pac4j.client.DelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.DefaultServicesManagerRegisteredServiceLocator;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.ServicesManagerConfigurationContext;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.WebUtils;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultDelegatedClientIdentityProviderRedirectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Delegation")
public class DefaultDelegatedClientIdentityProviderRedirectionStrategyTests {

    private ServicesManager servicesManager;

    private CasCookieBuilder casCookieBuilder;

    private static MockRequestContext getMockRequestContext() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        return context;
    }

    private static DelegatedClientIdentityProviderConfiguration getProviderConfiguration(final String client) {
        return DelegatedClientIdentityProviderConfiguration.builder()
            .name(client)
            .type("CasClient")
            .redirectUrl("https://localhost:8443/redirect")
            .build();
    }

    @BeforeEach
    public void setup() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val context = ServicesManagerConfigurationContext.builder()
            .serviceRegistry(new InMemoryServiceRegistry(appCtx))
            .applicationContext(appCtx)
            .environments(new HashSet<>(0))
            .servicesCache(Caffeine.newBuilder().build())
            .registeredServiceLocators(List.of(new DefaultServicesManagerRegisteredServiceLocator()))
            .build();
        this.servicesManager = new DefaultServicesManager(context);
        this.casCookieBuilder = mock(CasCookieBuilder.class);
    }

    @Test
    public void verifyExclusiveRedirect() {
        val strategy = getStrategy();
        val context = getMockRequestContext();
        val provider = getProviderConfiguration("SomeClient");

        val policy = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
        policy.setAllowedProviders(CollectionUtils.wrapList("SomeClient"));
        policy.setExclusive(true);
        configureService(policy);

        val service = RegisteredServiceTestUtils.getService();
        val results = strategy.getPrimaryDelegatedAuthenticationProvider(context, service, provider);
        assertFalse(results.isEmpty());
        assertTrue(results.get().isAutoRedirect());
        assertEquals(Ordered.LOWEST_PRECEDENCE, strategy.getOrder());
    }

    @Test
    public void verifyExistingPrimaryProvider() {
        val strategy = getStrategy();
        val context = getMockRequestContext();
        val provider = getProviderConfiguration("SomeClient");
        provider.setAutoRedirect(true);

        val policy = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
        configureService(policy);

        WebUtils.putDelegatedAuthenticationProviderPrimary(context, null);
        val results = strategy.getPrimaryDelegatedAuthenticationProvider(context, null, provider);
        assertFalse(results.isEmpty());
        assertTrue(results.get().isAutoRedirect());
    }

    @Test
    public void verifyPrimaryViaCookie() {
        val strategy = getStrategy();
        val context = getMockRequestContext();
        val provider = getProviderConfiguration("SomeClient");

        val policy = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
        configureService(policy);

        when(this.casCookieBuilder.retrieveCookieValue(any())).thenReturn("SomeClient");
        val results = strategy.getPrimaryDelegatedAuthenticationProvider(context, null, provider);
        assertFalse(results.isEmpty());
        assertTrue(results.get().isAutoRedirect());
    }

    private void configureService(final RegisteredServiceDelegatedAuthenticationPolicy policy) {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        val accessStrategy = new DefaultRegisteredServiceAccessStrategy();
        accessStrategy.setDelegatedAuthenticationPolicy(policy);
        registeredService.setAccessStrategy(accessStrategy);
        this.servicesManager.save(registeredService);
    }

    private DelegatedClientIdentityProviderRedirectionStrategy getStrategy() {
        var props = new CasConfigurationProperties();
        props.getAuthn().getPac4j().getCookie().setEnabled(true);
        return new DefaultDelegatedClientIdentityProviderRedirectionStrategy(servicesManager, casCookieBuilder, props);
    }
}
