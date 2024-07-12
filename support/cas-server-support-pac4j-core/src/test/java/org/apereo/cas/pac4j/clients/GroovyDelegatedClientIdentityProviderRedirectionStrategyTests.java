package org.apereo.cas.pac4j.clients;

import org.apereo.cas.BaseDelegatedAuthenticationTests;
import org.apereo.cas.pac4j.client.GroovyDelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyDelegatedClientIdentityProviderRedirectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Groovy")
class GroovyDelegatedClientIdentityProviderRedirectionStrategyTests extends BaseDelegatedAuthenticationTests {
    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val provider = DelegatedClientIdentityProviderConfiguration.builder()
            .name("SomeClient")
            .type("CasClient")
            .redirectUrl("https://localhost:8443/redirect")
            .build();
        val service = RegisteredServiceTestUtils.getService();
        val resource = new ClassPathResource("GroovyClientRedirectStrategy.groovy");

        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        val watchableScript = scriptFactory.fromResource(resource);
        val strategy = new GroovyDelegatedClientIdentityProviderRedirectionStrategy(servicesManager,
            watchableScript, applicationContext);
        assertFalse(strategy.select(context, service, Set.of(provider)).isEmpty());
        assertEquals(0, strategy.getOrder());
    }
}
