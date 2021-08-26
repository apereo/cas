package org.apereo.cas.pac4j.clients;

import org.apereo.cas.pac4j.client.GroovyDelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.DefaultServicesManagerRegisteredServiceLocator;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.ServicesManagerConfigurationContext;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;
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

/**
 * This is {@link GroovyDelegatedClientIdentityProviderRedirectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Groovy")
public class GroovyDelegatedClientIdentityProviderRedirectionStrategyTests {
    private ServicesManager servicesManager;

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
    }

    @Test
    public void verifyOperation() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val provider = DelegatedClientIdentityProviderConfiguration.builder()
            .name("SomeClient")
            .type("CasClient")
            .redirectUrl("https://localhost:8443/redirect")
            .build();
        val service = RegisteredServiceTestUtils.getService();
        val resource = new ClassPathResource("GroovyClientRedirectStrategy.groovy");
        val strategy = new GroovyDelegatedClientIdentityProviderRedirectionStrategy(this.servicesManager,
            new WatchableGroovyScriptResource(resource));
        assertFalse(strategy.getPrimaryDelegatedAuthenticationProvider(context, service, provider).isEmpty());
        assertEquals(0, strategy.getOrder());
    }
}
