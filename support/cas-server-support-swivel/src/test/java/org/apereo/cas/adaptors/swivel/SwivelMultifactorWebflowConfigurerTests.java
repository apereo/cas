package org.apereo.cas.adaptors.swivel;

import org.apereo.cas.adaptors.swivel.web.flow.SwivelMultifactorWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.BaseMultifactorWebflowConfigurerTests;

import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;

/**
 * This is {@link SwivelMultifactorWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseSwivelAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.swivel.swivelUrl=http://localhost:9191",
        "cas.authn.mfa.swivel.sharedSecret=$ecret",
        "cas.authn.mfa.swivel.ignoreSslErrors=true",
        "cas.authn.mfa.swivel.trusted-device-enabled=true",
        "cas.authn.mfa.trusted.deviceRegistrationEnabled=true"
    })
@Tag("Webflow")
public class SwivelMultifactorWebflowConfigurerTests extends BaseMultifactorWebflowConfigurerTests {
    @Autowired
    @Qualifier("swivelAuthenticatorFlowRegistry")
    private FlowDefinitionRegistry swivelAuthenticatorFlowRegistry;

    @Override
    protected FlowDefinitionRegistry getMultifactorFlowDefinitionRegistry() {
        return this.swivelAuthenticatorFlowRegistry;
    }

    @Override
    protected String getMultifactorEventId() {
        return SwivelMultifactorWebflowConfigurer.MFA_SWIVEL_EVENT_ID;
    }
}

