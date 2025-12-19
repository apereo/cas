package org.apereo.cas.pac4j.clients;

import module java.base;
import org.apereo.cas.BaseDelegatedAuthenticationTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.delegation.DelegationAutoRedirectTypes;
import org.apereo.cas.pac4j.client.DefaultDelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.pac4j.client.DelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.RegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.DelegationWebflowUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.Ordered;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultDelegatedClientIdentityProviderRedirectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Delegation")
@EnableConfigurationProperties(CasConfigurationProperties.class)
class DefaultDelegatedClientIdentityProviderRedirectionStrategyTests extends BaseDelegatedAuthenticationTests {
    private static DelegatedClientIdentityProviderConfiguration getProviderConfiguration(final String client) {
        return DelegatedClientIdentityProviderConfiguration.builder()
            .name(client)
            .type("CasClient")
            .redirectUrl("https://localhost:8443/redirect")
            .autoRedirectType(DelegationAutoRedirectTypes.SERVER)
            .build();
    }

    @Test
    void verifyDiscoveryStrategy() throws Throwable {
        val strategy = getStrategy();
        val context = MockRequestContext.create(applicationContext);
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
        val context = MockRequestContext.create(applicationContext);
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
        val context = MockRequestContext.create(applicationContext);
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
        val cookieBuilder = mock(CasCookieBuilder.class);
        when(cookieBuilder.retrieveCookieValue(any())).thenReturn("SomeClient");

        val strategy = getStrategy(cookieBuilder);
        val context = MockRequestContext.create(applicationContext);
        val provider = getProviderConfiguration("SomeClient");

        val policy = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
        configureService(policy);

        DelegationWebflowUtils.putDelegatedAuthenticationProviderPrimary(context, provider);

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
        return getStrategy(mock(CasCookieBuilder.class));
    }

    private DelegatedClientIdentityProviderRedirectionStrategy getStrategy(final CasCookieBuilder cookieBuilder) {
        return new DefaultDelegatedClientIdentityProviderRedirectionStrategy(servicesManager,
            cookieBuilder, casProperties, applicationContext);
    }
}
