package org.apereo.cas.consent;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link GroovyConsentActivationStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Groovy")
@TestPropertySource(properties = "cas.consent.activation-strategy-groovy-script.location=classpath:/ConsentActivationStrategy.groovy")
public class GroovyConsentActivationStrategyTests extends BaseConsentActivationStrategyTests {
}
