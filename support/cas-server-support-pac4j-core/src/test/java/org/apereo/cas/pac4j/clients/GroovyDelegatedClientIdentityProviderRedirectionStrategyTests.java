package org.apereo.cas.pac4j.clients;

import org.apereo.cas.pac4j.client.GroovyDelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.services.DefaultServicesManagerRegisteredServiceLocator;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.RegisteredServicesTemplatesManager;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.ServicesManagerConfigurationContext;
import org.apereo.cas.services.mgmt.DefaultServicesManager;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GroovyDelegatedClientIdentityProviderRedirectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Groovy")
class GroovyDelegatedClientIdentityProviderRedirectionStrategyTests {
    private ServicesManager servicesManager;

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
    }

    @Test
    void verifyOperation() throws Throwable {

        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val context = MockRequestContext.create(appCtx);

        val provider = DelegatedClientIdentityProviderConfiguration.builder()
            .name("SomeClient")
            .type("CasClient")
            .redirectUrl("https://localhost:8443/redirect")
            .build();
        val service = RegisteredServiceTestUtils.getService();
        val resource = new ClassPathResource("GroovyClientRedirectStrategy.groovy");
        val strategy = new GroovyDelegatedClientIdentityProviderRedirectionStrategy(this.servicesManager,
            new WatchableGroovyScriptResource(resource), appCtx);
        assertFalse(strategy.select(context, service, Set.of(provider)).isEmpty());
        assertEquals(0, strategy.getOrder());
    }
}
