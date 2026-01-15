package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SurrogateRegisteredServiceAccessStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RegisteredService")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
class SurrogateRegisteredServiceAccessStrategyTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifySurrogateDisabled() throws Throwable {
        val strategy = new SurrogateRegisteredServiceAccessStrategy();
        assertTrue(executeStrategy(strategy));
    }

    @Test
    void verifySurrogateDisabledWithAttributes() throws Throwable {
        val strategy = new SurrogateRegisteredServiceAccessStrategy();
        strategy.setSurrogateRequiredAttributes(CollectionUtils.wrap("surrogateA", "surrogateV"));
        assertFalse(executeStrategy(strategy));
    }

    @Test
    void verifySurrogateAttributesNotAvail() throws Throwable {
        val strategy = new SurrogateRegisteredServiceAccessStrategy();
        strategy.setSurrogateRequiredAttributes(CollectionUtils.wrap("surrogateA", "surrogateV",
            "surrogateB", "surrogateZ"));
        assertFalse(executeStrategy(strategy));
    }

    @Test
    void verifySurrogateAllowed() throws Throwable {
        val strategy = new SurrogateRegisteredServiceAccessStrategy();
        assertTrue(executeStrategy(strategy));
    }

    private boolean executeStrategy(final RegisteredServiceAccessStrategy strategy) throws Throwable {
        val request = RegisteredServiceAccessStrategyRequest.builder().applicationContext(applicationContext).principalId("casuser")
            .attributes(CollectionUtils.wrap(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, true))
            .build();
        return strategy.authorizeRequest(request);
    }
}
