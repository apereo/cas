package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.config.CoreSamlConfiguration;
import org.apereo.cas.config.DelegatedAuthenticationConfiguration;
import org.apereo.cas.config.DelegatedAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.config.DelegatedAuthenticationWebflowConfiguration;
import org.apereo.cas.config.PasswordlessAuthenticationConfiguration;
import org.apereo.cas.config.PasswordlessAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowConfigurer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link PasswordlessAuthenticationWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import({
    PasswordlessAuthenticationWebflowConfigurerTests.PasswordlessAuthenticationTestConfiguration.class,
    WebMvcAutoConfiguration.class,
    MockMvcAutoConfiguration.class,
    ErrorMvcAutoConfiguration.class,
    CoreSamlConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    DelegatedAuthenticationConfiguration.class,
    DelegatedAuthenticationEventExecutionPlanConfiguration.class,
    DelegatedAuthenticationWebflowConfiguration.class,
    PasswordlessAuthenticationConfiguration.class,
    PasswordlessAuthenticationWebflowConfiguration.class
})
@Tag("WebflowConfig")
class PasswordlessAuthenticationWebflowConfigurerTests extends BaseWebflowConfigurerTests {

    @Test
    void verifyOperation() throws Throwable {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);

        var state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_ACCEPT_PASSWORDLESS_AUTHENTICATION);
        assertNotNull(state);
        state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_PASSWORDLESS_DISPLAY);
        assertNotNull(state);
        state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_PASSWORDLESS_GET_USERID);
        assertNotNull(state);
        state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_PASSWORDLESS_VERIFY_ACCOUNT);
        assertNotNull(state);
        state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_PASSWORDLESS_DETERMINE_MFA);
        assertNotNull(state);
    }

    @TestConfiguration(value = "PasswordlessAuthenticationTestConfiguration", proxyBeanMethods = false)
    static class PasswordlessAuthenticationTestConfiguration {
        @Bean
        public CasMultifactorWebflowConfigurer dummyCasMultifactorWebflowConfigurer() {
            val registry = mock(FlowDefinitionRegistry.class);
            when(registry.getFlowDefinitionIds()).thenReturn(new String[]{"dummy"});
            val cfg = mock(CasMultifactorWebflowConfigurer.class);
            when(cfg.getMultifactorAuthenticationFlowDefinitionRegistries()).thenReturn(List.of(registry));
            return cfg;
        }
    }
}

