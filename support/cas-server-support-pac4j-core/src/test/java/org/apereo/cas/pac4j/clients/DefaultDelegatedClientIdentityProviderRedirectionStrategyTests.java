package org.apereo.cas.pac4j.clients;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.delegation.DelegationAutoRedirectTypes;
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
import org.apereo.cas.util.scripting.GroovyScriptResourceCacheManager;
import org.apereo.cas.util.scripting.ScriptResourceCacheManager;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.DelegationWebflowUtils;

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
import java.util.Set;

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
            .autoRedirectType(DelegationAutoRedirectTypes.SERVER)
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
    public void verifyDiscoveryStrategy() {

        val strategy = getStrategy();
        val context = getMockRequestContext();
        val provider = getProviderConfiguration("SomeClient");

        val policy = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
        policy.setSelectionStrategy("groovy { return providers.first() }");
        configureService(policy);

        val service = RegisteredServiceTestUtils.getService();
        val results = strategy.select(context, service, Set.of(provider));
        assertFalse(results.isEmpty());
        assertEquals(results.get().getName(), provider.getName());
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
        val results = strategy.select(context, service, Set.of(provider));
        assertFalse(results.isEmpty());
        assertSame(DelegationAutoRedirectTypes.SERVER, results.get().getAutoRedirectType());
        assertEquals(Ordered.LOWEST_PRECEDENCE, strategy.getOrder());
    }

    @Test
    public void verifyExistingPrimaryProvider() {
        val strategy = getStrategy();
        val context = getMockRequestContext();
        val provider = getProviderConfiguration("SomeClient");
        provider.setAutoRedirectType(DelegationAutoRedirectTypes.SERVER);

        val policy = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
        configureService(policy);

        DelegationWebflowUtils.putDelegatedAuthenticationProviderPrimary(context, null);
        val results = strategy.select(context, null, Set.of(provider));
        assertFalse(results.isEmpty());
        assertSame(DelegationAutoRedirectTypes.SERVER, results.get().getAutoRedirectType());
    }

    @Test
    public void verifyPrimaryViaCookie() {
        val strategy = getStrategy();
        val context = getMockRequestContext();
        val provider = getProviderConfiguration("SomeClient");

        val policy = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
        configureService(policy);

        DelegationWebflowUtils.putDelegatedAuthenticationProviderPrimary(context, provider);
        
        when(casCookieBuilder.retrieveCookieValue(any())).thenReturn("SomeClient");
        val results = strategy.select(context, null, Set.of(provider));
        assertFalse(results.isEmpty());
        assertSame(DelegationAutoRedirectTypes.SERVER, results.get().getAutoRedirectType());
    }

    private void configureService(final RegisteredServiceDelegatedAuthenticationPolicy policy) {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        val accessStrategy = new DefaultRegisteredServiceAccessStrategy();
        accessStrategy.setDelegatedAuthenticationPolicy(policy);
        registeredService.setAccessStrategy(accessStrategy);
        this.servicesManager.save(registeredService);
    }

    private DelegatedClientIdentityProviderRedirectionStrategy getStrategy() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton(ScriptResourceCacheManager.BEAN_NAME, GroovyScriptResourceCacheManager.class);
        applicationContext.refresh();
        ApplicationContextProvider.holdApplicationContext(applicationContext);

        var props = new CasConfigurationProperties();
        props.getAuthn().getPac4j().getCookie().setEnabled(true);
        return new DefaultDelegatedClientIdentityProviderRedirectionStrategy(servicesManager,
            casCookieBuilder, props, applicationContext);
    }
}
