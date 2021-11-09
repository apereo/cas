package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyMultifactorWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.BaseMultifactorWebflowConfigurerTests;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;

/**
 * This is {@link YubiKeyMultifactorWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseYubiKeyTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.yubikey.client-id=18423",
        "cas.authn.mfa.yubikey.secret-key=zAIqhjui12mK8x82oe9qzBEb0As=",
        "cas.authn.mfa.yubikey.json-file=file:${java.io.tmpdir}/yubikey.json",
        "cas.authn.mfa.yubikey.trusted-device-enabled=true",
        "cas.authn.mfa.yubikey.multiple-device-registration-enabled=true",
        "cas.authn.mfa.trusted.core.device-registration-enabled=true"
    })
@Tag("WebflowMfaConfig")
@Getter
public class YubiKeyMultifactorWebflowConfigurerTests extends BaseMultifactorWebflowConfigurerTests {
    @Autowired
    @Qualifier("yubikeyFlowRegistry")
    private FlowDefinitionRegistry multifactorFlowDefinitionRegistry;
    
    @Override
    protected String getMultifactorEventId() {
        return YubiKeyMultifactorWebflowConfigurer.MFA_YUBIKEY_EVENT_ID;
    }
}
