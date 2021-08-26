package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.web.config.CasBasicAuthenticationConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
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
    CasMultifactorAuthenticationWebflowConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class
})
@Tag("WebflowConfig")
public class BasicAuthenticationCasMultifactorWebflowCustomizerTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier("basicAuthenticationCasMultifactorWebflowCustomizer")
    private CasMultifactorWebflowCustomizer customizer;

    @Test
    public void verifyOperation() {
        assertFalse(customizer.getCandidateStatesForMultifactorAuthentication().isEmpty());
    }
}
