package org.apereo.cas.services;

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
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovySurrogateRegisteredServiceAccessStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("GroovyServices")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
class GroovySurrogateRegisteredServiceAccessStrategyTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifySurrogateDisabled() throws Throwable {
        val strategy = new GroovySurrogateRegisteredServiceAccessStrategy();
        strategy.setGroovyScript("classpath:/surrogate-access.groovy");
        assertFalse(executeStrategy("casuser-disabled", true, strategy));
    }

    private boolean executeStrategy(final String principal, final boolean surrogate,
                                    final GroovySurrogateRegisteredServiceAccessStrategy strategy) throws Throwable {
        val request = RegisteredServiceAccessStrategyRequest.builder().applicationContext(applicationContext).principalId(principal)
            .attributes(surrogate ? CollectionUtils.wrap(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, true) : Map.of())
            .build();
        return strategy.authorizeRequest(request);
    }

    @Test
    void verifySurrogateFails() throws Throwable {
        val strategy = new GroovySurrogateRegisteredServiceAccessStrategy();
        strategy.setGroovyScript("classpath:/surrogate-access.groovy");
        assertFalse(executeStrategy("casuser-fail", true, strategy));
    }

    @Test
    void verifySurrogateAllowed() throws Throwable {
        val strategy = new GroovySurrogateRegisteredServiceAccessStrategy();
        strategy.setGroovyScript("classpath:/surrogate-access.groovy");
        assertTrue(executeStrategy("casuser-enabled", true, strategy));
    }

    @Test
    void verifyNoSurrogateSession() throws Throwable {
        val strategy = new GroovySurrogateRegisteredServiceAccessStrategy();
        strategy.setGroovyScript("classpath:/surrogate-access.groovy");
        assertTrue(executeStrategy("casuser", false, strategy));
    }
}
