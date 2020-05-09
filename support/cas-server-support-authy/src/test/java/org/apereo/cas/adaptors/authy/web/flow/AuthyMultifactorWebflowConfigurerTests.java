package org.apereo.cas.adaptors.authy.web.flow;

import org.apereo.cas.adaptors.authy.BaseAuthyAuthenticationTests;
import org.apereo.cas.web.flow.configurer.BaseMultifactorWebflowConfigurerTests;

import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;

/**
 * This is {@link AuthyMultifactorWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseAuthyAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.authy.trusted-device-enabled=true",
        "cas.authn.mfa.trusted.deviceRegistrationEnabled=true",
        "cas.authn.mfa.authy.apiKey=example",
        "cas.authn.mfa.authy.apiUrl=http://localhost:8080/authy"
    })
@Tag("Webflow")
public class AuthyMultifactorWebflowConfigurerTests extends BaseMultifactorWebflowConfigurerTests {
    @Autowired
    @Qualifier("authyAuthenticatorFlowRegistry")
    private FlowDefinitionRegistry authyAuthenticatorFlowRegistry;

    @Override
    protected FlowDefinitionRegistry getMultifactorFlowDefinitionRegistry() {
        return this.authyAuthenticatorFlowRegistry;
    }

    @Override
    protected String getMultifactorEventId() {
        return AuthyMultifactorWebflowConfigurer.MFA_AUTHY_EVENT_ID;
    }
}


