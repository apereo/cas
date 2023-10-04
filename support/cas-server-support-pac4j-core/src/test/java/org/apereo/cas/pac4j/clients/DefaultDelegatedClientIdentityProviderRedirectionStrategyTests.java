package org.apereo.cas.pac4j.clients;

import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.delegation.DelegationAutoRedirectTypes;
import org.apereo.cas.pac4j.client.DefaultDelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.pac4j.client.DelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.DefaultServicesManagerRegisteredServiceLocator;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.RegisteredServicesTemplatesManager;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.ServicesManagerConfigurationContext;
import org.apereo.cas.services.mgmt.DefaultServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.DelegationWebflowUtils;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.Ordered;
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
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasCoreUtilConfiguration.class
}, properties = "cas.authn.pac4j.cookie.enabled=true")
@EnableConfigurationProperties(CasConfigurationProperties.class)
class DefaultDelegatedClientIdentityProviderRedirectionStrategyTests {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    private ServicesManager servicesManager;

    private CasCookieBuilder casCookieBuilder;

    private static MockRequestContext getMockRequestContext() throws Exception {
        return org.apereo.cas.util.MockRequestContext.create();
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
            .registeredServicesTemplatesManager(mock(RegisteredServicesTemplatesManager.class))
            .environments(new HashSet<>(0))
            .servicesCache(Caffeine.newBuilder().build())
            .registeredServiceLocators(List.of(new DefaultServicesManagerRegisteredServiceLocator()))
            .build();
        this.servicesManager = new DefaultServicesManager(context);
        this.casCookieBuilder = mock(CasCookieBuilder.class);
    }

    @Test
    void verifyDiscoveryStrategy() throws Throwable {

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
    void verifyExclusiveRedirect() throws Throwable {
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
    void verifyExistingPrimaryProvider() throws Throwable {
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
    void verifyPrimaryViaCookie() throws Throwable {
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
        return new DefaultDelegatedClientIdentityProviderRedirectionStrategy(servicesManager,
            casCookieBuilder, casProperties, applicationContext);
    }
}
