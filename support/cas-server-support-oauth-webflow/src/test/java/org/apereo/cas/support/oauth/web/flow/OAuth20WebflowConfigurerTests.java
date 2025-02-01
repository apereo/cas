package org.apereo.cas.support.oauth.web.flow;

import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.engine.Flow;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20WebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import(BaseOAuth20WebflowTests.SharedTestConfiguration.class)
@Tag("OAuthWeb")
class OAuth20WebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier("oauth20LogoutWebflowConfigurer")
    private CasWebflowConfigurer oauth20LogoutWebflowConfigurer;

    @Test
    void verifyOperation() {
        assertNotNull(oauth20LogoutWebflowConfigurer);
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
    }
}

