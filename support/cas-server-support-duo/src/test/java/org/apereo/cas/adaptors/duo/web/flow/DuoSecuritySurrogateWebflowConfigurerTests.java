package org.apereo.cas.adaptors.duo.web.flow;

import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasDuoSecurityAutoConfiguration;
import org.apereo.cas.config.CasSurrogateAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasSurrogateAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;
import static org.apereo.cas.web.flow.CasWebflowConstants.STATE_ID_DUO_UNIVERSAL_PROMPT_VALIDATE_LOGIN;
import static org.apereo.cas.web.flow.CasWebflowConstants.STATE_ID_LOAD_SURROGATES_ACTION;
import static org.apereo.cas.web.flow.CasWebflowConstants.STATE_ID_SELECT_SURROGATE;
import static org.apereo.cas.web.flow.CasWebflowConstants.STATE_ID_SURROGATE_VIEW;
import static org.apereo.cas.web.flow.CasWebflowConstants.TRANSITION_ID_SUCCESS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DuoSecuritySurrogateWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("DuoSecurity")
class DuoSecuritySurrogateWebflowConfigurerTests {

    @ImportAutoConfiguration({
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasSurrogateAuthenticationAutoConfiguration.class,
        CasSurrogateAuthenticationWebflowAutoConfiguration.class
    })
    public static class SharedTestAutoConfiguration {
    }

    @Nested
    @ImportAutoConfiguration(SharedTestAutoConfiguration.class)
    class DefaultTests extends BaseWebflowConfigurerTests {

        @Test
        void verifyOperation() {
            assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
            val flow = (Flow) this.flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
            assertNotNull(flow);
            var state = (TransitionableState) flow.getState(STATE_ID_LOAD_SURROGATES_ACTION);
            assertNotNull(state);
            state = (TransitionableState) flow.getState(STATE_ID_SELECT_SURROGATE);
            assertNotNull(state);
            state = (TransitionableState) flow.getState(STATE_ID_SURROGATE_VIEW);
            assertNotNull(state);
        }
    }

    @Nested
    @ImportAutoConfiguration({
        CasDuoSecurityAutoConfiguration.class,
        SharedTestAutoConfiguration.class
    })
    @TestPropertySource(properties = {
        "cas.authn.mfa.duo[0].duo-secret-key=aGKL0OndjtknbnVOWaFKosbbinNFEKXHxgXCJEBz",
        "cas.authn.mfa.duo[0].duo-integration-key=DIOXVRQD3UMZ8XXMNFQ8",
        "cas.authn.mfa.duo[0].duo-api-host=theapi.duosecurity.com"
    })
    class DuoSecurityUniversalPromptTests extends BaseWebflowConfigurerTests {
        @Autowired
        @Qualifier("surrogateDuoSecurityMultifactorAuthenticationWebflowConfigurer")
        private CasWebflowConfigurer surrogateDuoSecurityMultifactorAuthenticationWebflowConfigurer;

        @Autowired
        @Qualifier("surrogateDuoSecurityMultifactorWebflowCustomizer")
        private CasMultifactorWebflowCustomizer surrogateDuoSecurityMultifactorWebflowCustomizer;

        @Test
        void verifyOperation() {
            assertNotNull(surrogateDuoSecurityMultifactorAuthenticationWebflowConfigurer);
            assertNotNull(surrogateDuoSecurityMultifactorWebflowCustomizer);
            val flow = (Flow) this.flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
            assertNotNull(flow);
            var state = (TransitionableState) flow.getState(STATE_ID_DUO_UNIVERSAL_PROMPT_VALIDATE_LOGIN);
            assertEquals(STATE_ID_LOAD_SURROGATES_ACTION, state.getTransition(TRANSITION_ID_SUCCESS).getTargetStateId());

            val mappings = surrogateDuoSecurityMultifactorWebflowCustomizer.getWebflowAttributeMappings();
            assertTrue(mappings.contains(WebUtils.REQUEST_SURROGATE_ACCOUNT_ATTRIBUTE));
        }
    }
}
