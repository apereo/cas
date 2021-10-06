package org.apereo.cas.consent;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyConsentActivationStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Groovy")
@TestPropertySource(properties = {
    "cas.consent.core.crypto.enabled=false",
    "cas.consent.activation-strategy-groovy-script.location=classpath:/ConsentActivationStrategy.groovy"
})
public class GroovyConsentActivationStrategyTests extends BaseConsentActivationStrategyTests {
    @Autowired
    @Qualifier(ConsentActivationStrategy.BEAN_NAME)
    private ConsentActivationStrategy consentActivationStrategy;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyDestroyOperation() throws Exception {
        assertNotNull(consentActivationStrategy);
        applicationContext.getBeanFactory().destroyBean(consentActivationStrategy);
    }
}
