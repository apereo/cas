package org.apereo.cas.pm.web.flow;

import module java.base;
import org.apereo.cas.config.CasPasswordManagementAutoConfiguration;
import org.apereo.cas.config.CasPasswordManagementWebflowAutoConfiguration;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.Flow;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PasswordManagementAccountProfileWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
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
class PasswordManagementAccountProfileWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    void verifyOperation() {
        val flow = (Flow) flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_ACCOUNT);
        assertNotNull(flow);
        assertTrue(Arrays.stream(flow.getStartActionList().toArray())
            .anyMatch(ac -> ac.toString().contains(CasWebflowConstants.ACTION_ID_PREPARE_ACCOUNT_PASSWORD_MANAGEMENT)));
    }
}
