package org.apereo.cas.web.flow;

import org.apereo.cas.web.flow.config.CasWebflowAccountProfileConfiguration;
import org.apereo.cas.web.flow.configurer.acct.MyAccountProfileWebflowConfigurer;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MyAccountProfileWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("WebflowConfig")
@TestPropertySource(properties = "CasFeatureModule.AccountManagement.enabled=true")
@Import(CasWebflowAccountProfileConfiguration.class)
public class MyAccountProfileWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier("accountProfileFlowRegistry")
    protected FlowDefinitionRegistry accountProfileFlowRegistry;

    @Test
    public void verifyOperation() throws Exception {
        val flow = (Flow) accountProfileFlowRegistry.getFlowDefinition(MyAccountProfileWebflowConfigurer.FLOW_ID);
        assertNotNull(flow);
    }
}
