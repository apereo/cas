package org.apereo.cas.mfa.simple.web.flow;

import org.apereo.cas.config.SurrogateAuthenticationAuditConfiguration;
import org.apereo.cas.config.SurrogateAuthenticationConfiguration;
import org.apereo.cas.config.SurrogateAuthenticationWebflowConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.simple.BaseCasSimpleMultifactorAuthenticationTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.BaseMultifactorWebflowConfigurerTests;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;

import static org.apereo.cas.web.flow.CasWebflowConstants.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasSimpleMultifactorWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowMfaConfig")
public class CasSimpleMultifactorWebflowConfigurerTests {

    @SpringBootTest(classes = BaseCasSimpleMultifactorAuthenticationTests.SharedTestConfiguration.class,
        properties = {
            "cas.authn.mfa.simple.trusted-device-enabled=true",
            "cas.authn.mfa.trusted.core.device-registration-enabled=true"
        })
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @Getter
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class DefaultTests extends BaseMultifactorWebflowConfigurerTests {
        @Autowired
        @Qualifier("mfaSimpleAuthenticatorFlowRegistry")
        private FlowDefinitionRegistry multifactorFlowDefinitionRegistry;

        @Override
        protected String getMultifactorEventId() {
            return CasSimpleMultifactorWebflowConfigurer.MFA_SIMPLE_FLOW_ID;
        }
    }

    @SpringBootTest(classes = {
        SurrogateAuthenticationConfiguration.class,
        SurrogateAuthenticationAuditConfiguration.class,
        SurrogateAuthenticationWebflowConfiguration.class,
        BaseCasSimpleMultifactorAuthenticationTests.SharedTestConfiguration.class
    })
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @Getter
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class SurrogateTests extends BaseMultifactorWebflowConfigurerTests {
        @Autowired
        @Qualifier("mfaSimpleAuthenticatorFlowRegistry")
        private FlowDefinitionRegistry multifactorFlowDefinitionRegistry;

        @Override
        protected String getMultifactorEventId() {
            return CasSimpleMultifactorWebflowConfigurer.MFA_SIMPLE_FLOW_ID;
        }

        @Test
        public void verifySurrogateOperation() {
            val flow = (Flow) loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
            var state = (TransitionableState) flow.getState(CasSimpleMultifactorWebflowConfigurer.MFA_SIMPLE_FLOW_ID);
            assertEquals(STATE_ID_LOAD_SURROGATES_ACTION, state.getTransition(TRANSITION_ID_SUCCESS).getTargetStateId());
        }
    }
}

