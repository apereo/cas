package org.apereo.cas.pm.web.flow;

import org.apereo.cas.pm.config.PasswordManagementConfiguration;
import org.apereo.cas.pm.config.PasswordManagementWebflowConfiguration;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PasswordManagementWebflowConfigurerDisabledTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import({
    PasswordManagementConfiguration.class,
    PasswordManagementWebflowConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class
})
@TestPropertySource(properties = "cas.authn.pm.enabled=false")
@Tag("Webflow")
public class PasswordManagementWebflowConfigurerDisabledTests extends BaseWebflowConfigurerTests {

    @Test
    public void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        var state = (TransitionableState) flow.getState(CasWebflowConstants.VIEW_ID_AUTHENTICATION_BLOCKED);
        assertNotNull(state);
        state = (TransitionableState) flow.getState(CasWebflowConstants.VIEW_ID_INVALID_WORKSTATION);
        assertNotNull(state);
        state = (TransitionableState) flow.getState(CasWebflowConstants.VIEW_ID_INVALID_AUTHENTICATION_HOURS);
        assertNotNull(state);
        state = (TransitionableState) flow.getState(CasWebflowConstants.VIEW_ID_ACCOUNT_LOCKED);
        assertNotNull(state);
        state = (TransitionableState) flow.getState(CasWebflowConstants.VIEW_ID_ACCOUNT_DISABLED);
        assertNotNull(state);
        state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_PASSWORD_UPDATE_SUCCESS);
        assertNotNull(state);

        verifyPasswordManagementStates(flow);
    }

    protected void verifyPasswordManagementStates(final Flow flow) {
        var state = (TransitionableState) flow.getState(CasWebflowConstants.VIEW_ID_EXPIRED_PASSWORD);
        assertNotNull(state);
        state = (TransitionableState) flow.getState(CasWebflowConstants.VIEW_ID_MUST_CHANGE_PASSWORD);
        assertNotNull(state);
    }
}

