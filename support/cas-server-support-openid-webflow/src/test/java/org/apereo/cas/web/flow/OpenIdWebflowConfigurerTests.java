package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasThymeleafConfiguration;
import org.apereo.cas.config.OpenIdConfiguration;
import org.apereo.cas.services.web.config.CasThemesConfiguration;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;
import org.apereo.cas.web.config.CasValidationConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.OpenIdWebflowConfiguration;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.engine.Flow;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OpenIdWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 * @deprecated 6.2
 */
@Import({
    BaseWebflowConfigurerTests.SharedTestConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasCoreValidationConfiguration.class,
    CasValidationConfiguration.class,
    CasThemesConfiguration.class,
    CasThymeleafConfiguration.class,
    OpenIdConfiguration.class,
    OpenIdWebflowConfiguration.class
})
@Tag("Webflow")
@Deprecated(since = "6.2.0")
public class OpenIdWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    public void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_OPEN_ID_SINGLE_SIGN_ON_ACTION));
        assertTrue(flow.containsState(CasWebflowConstants.DECISION_STATE_OPEN_ID_SELECT_FIRST_ACTION));
    }
}
