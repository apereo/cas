package org.apereo.cas.support.saml.mdui.web.flow;

import org.apereo.cas.config.CoreSamlConfiguration;
import org.apereo.cas.support.saml.mdui.config.SamlMetadataUIConfiguration;
import org.apereo.cas.support.saml.mdui.config.SamlMetadataUIWebflowConfiguration;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.engine.Flow;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlMetadataUIWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import({
    CoreSamlConfiguration.class,
    SamlMetadataUIConfiguration.class,
    SamlMetadataUIWebflowConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class
})
@Tag("SAML")
public class SamlMetadataUIWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    public void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
    }
}
