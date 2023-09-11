package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasBasicAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BasicAuthenticationCasMultifactorWebflowCustomizerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Import({
    CasBasicAuthenticationConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class
})
@Tag("WebflowConfig")
class BasicAuthenticationCasMultifactorWebflowCustomizerTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier("basicAuthenticationCasMultifactorWebflowCustomizer")
    private CasMultifactorWebflowCustomizer customizer;

    @Test
    void verifyOperation() throws Throwable {
        assertFalse(customizer.getCandidateStatesForMultifactorAuthentication().isEmpty());
    }
}
