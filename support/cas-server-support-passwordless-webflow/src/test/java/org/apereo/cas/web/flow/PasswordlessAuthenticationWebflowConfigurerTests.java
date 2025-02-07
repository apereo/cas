package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreSamlAutoConfiguration;
import org.apereo.cas.config.CasDelegatedAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasPasswordlessAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasPasswordlessAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.execution.Action;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link PasswordlessAuthenticationWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import(PasswordlessAuthenticationWebflowConfigurerTests.PasswordlessAuthenticationTestConfiguration.class)
@SpringBootTestAutoConfigurations
@ImportAutoConfiguration({
    CasCoreSamlAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasDelegatedAuthenticationAutoConfiguration.class,
    CasPasswordlessAuthenticationAutoConfiguration.class,
    CasPasswordlessAuthenticationWebflowAutoConfiguration.class
})
@Tag("WebflowConfig")
@TestPropertySource(properties = {
    "cas.authn.passwordless.google-recaptcha.enabled=true",
    "cas.authn.passwordless.google-recaptcha.site-key=${#uuid}"
})
class PasswordlessAuthenticationWebflowConfigurerTests extends BaseWebflowConfigurerTests {

    @Autowired
    @Qualifier("passwordlessInitializeCaptchaAction")
    private Action passwordlessInitializeCaptchaAction;

    @Test
    void verifyOperation() throws Throwable {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
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

        val requestContext = MockRequestContext.create(applicationContext);
        val event = passwordlessInitializeCaptchaAction.execute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        assertNotNull(WebUtils.getRecaptchaSiteKey(requestContext));
        assertTrue(PasswordlessWebflowUtils.isPasswordlessCaptchaEnabled(requestContext));
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

