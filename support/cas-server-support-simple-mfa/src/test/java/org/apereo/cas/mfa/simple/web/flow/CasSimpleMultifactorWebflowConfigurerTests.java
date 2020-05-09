package org.apereo.cas.mfa.simple.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.simple.BaseCasSimpleMultifactorAuthenticationTests;
import org.apereo.cas.web.flow.configurer.BaseMultifactorWebflowConfigurerTests;

import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;

/**
 * This is {@link CasSimpleMultifactorWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseCasSimpleMultifactorAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.simple.trusted-device-enabled=true",
        "cas.authn.mfa.trusted.deviceRegistrationEnabled=true"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Webflow")
public class CasSimpleMultifactorWebflowConfigurerTests extends BaseMultifactorWebflowConfigurerTests {
    @Autowired
    @Qualifier("mfaSimpleAuthenticatorFlowRegistry")
    private FlowDefinitionRegistry mfaSimpleAuthenticatorFlowRegistry;

    @Override
    protected FlowDefinitionRegistry getMultifactorFlowDefinitionRegistry() {
        return this.mfaSimpleAuthenticatorFlowRegistry;
    }

    @Override
    protected String getMultifactorEventId() {
        return CasSimpleMultifactorWebflowConfigurer.MFA_SIMPLE_EVENT_ID;
    }
}

