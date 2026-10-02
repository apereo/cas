package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreSamlAutoConfiguration;
import org.apereo.cas.config.CasDelegatedAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasPasswordlessAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasPasswordlessAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PasswordlessAuthenticationPasskeyWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
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
@TestPropertySource(properties = "cas.authn.mfa.web-authn.core.allow-primary-authentication=true")
class PasswordlessAuthenticationPasskeyWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    void verifyPasskeyTransitions() {
        val flow = (Flow) flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        for (val stateId : List.of(CasWebflowConstants.STATE_ID_PASSWORDLESS_GET_USERID,
            CasWebflowConstants.STATE_ID_PASSWORDLESS_DISPLAY_SELECTION_MENU)) {
            val state = (TransitionableState) flow.getState(stateId);
            val transition = state.getTransition(CasWebflowConstants.TRANSITION_ID_VALIDATE);
            assertNotNull(transition);
            assertEquals(CasWebflowConstants.STATE_ID_WEBAUTHN_VALIDATE, transition.getTargetStateId());
        }
    }
}
