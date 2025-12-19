package org.apereo.cas.pm.web.flow;

import module java.base;
import org.apereo.cas.config.CasPasswordManagementAutoConfiguration;
import org.apereo.cas.config.CasPasswordManagementWebflowAutoConfiguration;
import org.apereo.cas.trusted.util.MultifactorAuthenticationTrustUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.ActionState;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PasswordManagementMultifactorTrustWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@ImportAutoConfiguration({
    CasPasswordManagementAutoConfiguration.class,
    CasPasswordManagementWebflowAutoConfiguration.class
})
@Tag("WebflowConfig")
@TestPropertySource(properties = {
    "cas.authn.pm.core.enabled=true",
    "CasFeatureModule.AccountManagement.enabled=true"
})
class PasswordManagementMultifactorTrustWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    void verifyOperation() throws Exception {
        val requestContext = MockRequestContext.create(applicationContext);
        val flow = flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_PASSWORD_RESET);
        val state = (ActionState) flow.getState(CasWebflowConstants.STATE_ID_INIT_PASSWORD_RESET);
        for (val action : state.getExitActionList()) {
            action.execute(requestContext);
        }
        assertFalse(MultifactorAuthenticationWebflowUtils.isMultifactorDeviceRegistrationEnabled(requestContext));
        assertTrue(MultifactorAuthenticationTrustUtils.isMultifactorAuthenticationTrustedDevicesDisabled(requestContext));
    }
}
