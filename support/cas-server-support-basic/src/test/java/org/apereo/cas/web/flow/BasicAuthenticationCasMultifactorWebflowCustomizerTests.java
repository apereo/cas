package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasBasicAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BasicAuthenticationCasMultifactorWebflowCustomizerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@ImportAutoConfiguration({
    CasBasicAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class
})
@Tag("WebflowConfig")
class BasicAuthenticationCasMultifactorWebflowCustomizerTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier("basicAuthenticationCasMultifactorWebflowCustomizer")
    private CasMultifactorWebflowCustomizer customizer;

    @Test
    void verifyOperation() {
        assertFalse(customizer.getCandidateStatesForMultifactorAuthentication().isEmpty());
    }
}
