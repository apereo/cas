package org.apereo.cas.adaptors.authy.web.flow;

import org.apereo.cas.adaptors.authy.BaseAuthyAuthenticationTests;
import org.apereo.cas.web.flow.configurer.BaseMultifactorWebflowConfigurerTests;

import lombok.Getter;
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
        "cas.authn.mfa.trusted.core.device-registration-enabled=true",
        "cas.authn.mfa.authy.api-key=example",
        "cas.authn.mfa.authy.api-url=http://localhost:8080/authy"
    })
@Tag("WebflowMfaConfig")
@Getter
public class AuthyMultifactorWebflowConfigurerTests extends BaseMultifactorWebflowConfigurerTests {
    @Autowired
    @Qualifier("authyAuthenticatorFlowRegistry")
    private FlowDefinitionRegistry multifactorFlowDefinitionRegistry;

    @Override
    protected String getMultifactorEventId() {
        return AuthyMultifactorWebflowConfigurer.MFA_AUTHY_EVENT_ID;
    }
}


