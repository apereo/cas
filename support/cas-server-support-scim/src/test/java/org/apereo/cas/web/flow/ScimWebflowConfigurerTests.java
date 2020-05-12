package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasScimConfiguration;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.Flow;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ScimWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import({
    CasScimConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class
})
@TestPropertySource(properties = "cas.scim.target=http://localhost:8218")
@Tag("Webflow")
public class ScimWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    public void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
    }
}
