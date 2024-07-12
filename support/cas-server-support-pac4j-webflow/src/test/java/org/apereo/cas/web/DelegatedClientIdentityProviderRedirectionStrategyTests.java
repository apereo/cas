package org.apereo.cas.web;

import org.apereo.cas.pac4j.client.DelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientIdentityProviderRedirectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class,
    properties = "cas.authn.pac4j.core.groovy-redirection-strategy.location=classpath:/GroovyClientRedirectStrategy.groovy")
@Tag("Delegation")
@ExtendWith(CasTestExtension.class)
class DelegatedClientIdentityProviderRedirectionStrategyTests {
    @Autowired
    @Qualifier("delegatedClientIdentityProviderRedirectionStrategy")
    private DelegatedClientIdentityProviderRedirectionStrategy delegatedClientIdentityProviderRedirectionStrategy;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val provider = DelegatedClientIdentityProviderConfiguration.builder()
            .name("SomeClient")
            .type("CasClient")
            .redirectUrl("https://localhost:8443/redirect")
            .build();
        val result = delegatedClientIdentityProviderRedirectionStrategy.select(context, null, Set.of(provider));
        assertTrue(result.isEmpty());
    }
}
