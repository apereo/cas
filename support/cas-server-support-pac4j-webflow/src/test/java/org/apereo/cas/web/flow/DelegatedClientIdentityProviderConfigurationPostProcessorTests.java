package org.apereo.cas.web.flow;

import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.execution.RequestContextHolder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientIdentityProviderConfigurationPostProcessorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
@Tag("Delegation")
class DelegatedClientIdentityProviderConfigurationPostProcessorTests {
    @Autowired
    @Qualifier("delegatedClientIdentityProviderConfigurationPostProcessor")
    private DelegatedClientIdentityProviderConfigurationPostProcessor delegatedClientIdentityProviderConfigurationPostProcessor;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(delegatedClientIdentityProviderConfigurationPostProcessor);
        assertDoesNotThrow(() -> {
            val context = MockRequestContext.create(applicationContext);
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());
            delegatedClientIdentityProviderConfigurationPostProcessor.process(context, Set.of());
            delegatedClientIdentityProviderConfigurationPostProcessor.destroy();
        });

    }
}
