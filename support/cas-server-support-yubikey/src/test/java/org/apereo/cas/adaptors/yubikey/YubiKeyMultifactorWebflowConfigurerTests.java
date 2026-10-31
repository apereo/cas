package org.apereo.cas.adaptors.yubikey;

import module java.base;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAccountProfileWebflowConfigurer;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyMultifactorWebflowConfigurer;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.BaseMultifactorWebflowConfigurerTests;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import static org.junit.jupiter.api.Assertions.*;

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
        "cas.authn.mfa.yubikey.json.location=file:${java.io.tmpdir}/yubikey.json",
        "cas.authn.mfa.yubikey.trusted-device-enabled=true",
        "cas.authn.mfa.yubikey.multiple-device-registration-enabled=true",
        "cas.authn.mfa.trusted.core.device-registration-enabled=true",
        "CasFeatureModule.AccountManagement.enabled=true"
    })
@Tag("WebflowMfaConfig")
@ExtendWith(CasTestExtension.class)
@Getter
class YubiKeyMultifactorWebflowConfigurerTests extends BaseMultifactorWebflowConfigurerTests {
    @Autowired
    @Qualifier("yubikeyFlowRegistry")
    private FlowDefinitionRegistry multifactorFlowDefinitionRegistry;
    
    @Override
    protected String getMultifactorEventId() {
        return YubiKeyMultifactorWebflowConfigurer.MFA_YUBIKEY_EVENT_ID;
    }

    @Test
    void verifyAccountProfileRegistration() {
        val flow = (Flow) flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_ACCOUNT);
        val accountView = (ViewState) flow.getState(CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW);
        assertTrue(Arrays.stream(accountView.getRenderActionList().toArray())
            .anyMatch(action -> action.toString().contains(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_YUBIKEY_MFA_PREPARE)));
        assertNotNull(accountView.getTransition(YubiKeyAccountProfileWebflowConfigurer.TRANSITION_ID_REGISTER_YUBIKEY));
        assertNotNull(flow.getState(CasWebflowConstants.STATE_ID_YUBIKEY_VIEW_REGISTRATION));
        assertNotNull(flow.getState(CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_YUBIKEY_REGISTRATION_FINALIZED));
    }
}
