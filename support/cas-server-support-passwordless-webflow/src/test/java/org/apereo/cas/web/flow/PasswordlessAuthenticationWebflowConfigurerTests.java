package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CoreSamlConfiguration;
import org.apereo.cas.config.PasswordlessAuthenticationConfiguration;
import org.apereo.cas.config.PasswordlessAuthenticationWebflowConfiguration;
import org.apereo.cas.support.pac4j.config.Pac4jDelegatedAuthenticationConfiguration;
import org.apereo.cas.support.pac4j.config.support.authentication.Pac4jAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.DelegatedAuthenticationWebflowConfiguration;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PasswordlessAuthenticationWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import({
    BaseWebflowConfigurerTests.SharedTestConfiguration.class,
    WebMvcAutoConfiguration.class,
    MockMvcAutoConfiguration.class,
    ErrorMvcAutoConfiguration.class,
    CoreSamlConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    Pac4jDelegatedAuthenticationConfiguration.class,
    Pac4jAuthenticationEventExecutionPlanConfiguration.class,
    DelegatedAuthenticationWebflowConfiguration.class,
    PasswordlessAuthenticationConfiguration.class,
    PasswordlessAuthenticationWebflowConfiguration.class
})
@Tag("Webflow")
public class PasswordlessAuthenticationWebflowConfigurerTests extends BaseWebflowConfigurerTests {

    @Test
    public void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);

        var state = (TransitionableState) flow.getState(PasswordlessAuthenticationWebflowConfigurer.STATE_ID_ACCEPT_PASSWORDLESS_AUTHENTICATION);
        assertNotNull(state);
        state = (TransitionableState) flow.getState(PasswordlessAuthenticationWebflowConfigurer.STATE_ID_PASSWORDLESS_DISPLAY);
        assertNotNull(state);
        state = (TransitionableState) flow.getState(PasswordlessAuthenticationWebflowConfigurer.STATE_ID_PASSWORDLESS_GET_USERID);
        assertNotNull(state);
        state = (TransitionableState) flow.getState(PasswordlessAuthenticationWebflowConfigurer.STATE_ID_PASSWORDLESS_VERIFY_ACCOUNT);
        assertNotNull(state);
        state = (TransitionableState) flow.getState(PasswordlessAuthenticationWebflowConfigurer.STATE_ID_PASSWORDLESS_DETERMINE_MFA);
        assertNotNull(state);
    }
}

