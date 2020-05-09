package org.apereo.cas.gauth.web.flow;

import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
import org.apereo.cas.web.flow.configurer.BaseMultifactorWebflowConfigurerTests;

import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;

/**
 * This is {@link GoogleAuthenticatorMultifactorWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseGoogleAuthenticatorTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.gauth.json.location=classpath:/repository.json",
        "cas.authn.mfa.gauth.trusted-device-enabled=true",
        "cas.authn.mfa.trusted.deviceRegistrationEnabled=true"
    })
@Tag("Webflow")
public class GoogleAuthenticatorMultifactorWebflowConfigurerTests extends BaseMultifactorWebflowConfigurerTests {
    @Autowired
    @Qualifier("googleAuthenticatorFlowRegistry")
    private FlowDefinitionRegistry googleAuthenticatorFlowRegistry;

    @Override
    protected FlowDefinitionRegistry getMultifactorFlowDefinitionRegistry() {
        return this.googleAuthenticatorFlowRegistry;
    }

    @Override
    protected String getMultifactorEventId() {
        return GoogleAuthenticatorMultifactorWebflowConfigurer.MFA_GAUTH_EVENT_ID;
    }
}

