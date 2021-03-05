package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.web.flow.configurer.BaseMultifactorWebflowConfigurerTests;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;

/**
 * This is {@link WebAuthnMultifactorWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.web-authn.allowed-origins=https://localhost:8443",
        "cas.authn.mfa.web-authn.application-id=https://localhost:8443",
        "cas.authn.mfa.web-authn.relying-party-name=CAS WebAuthn Demo",
        "cas.authn.mfa.web-authn.relying-party-id=example.org",
        "cas.authn.mfa.web-authn.allow-primary-authentication=true"
    })
@Tag("WebflowConfig")
@Getter
public class WebAuthnMultifactorWebflowConfigurerTests extends BaseMultifactorWebflowConfigurerTests {
    @Autowired
    @Qualifier("webAuthnFlowRegistry")
    private FlowDefinitionRegistry multifactorFlowDefinitionRegistry;

    @Override
    protected String getMultifactorEventId() {
        return WebAuthnMultifactorWebflowConfigurer.MFA_WEB_AUTHN_EVENT_ID;
    }
}
