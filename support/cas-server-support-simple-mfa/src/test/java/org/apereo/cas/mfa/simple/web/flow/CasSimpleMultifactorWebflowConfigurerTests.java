package org.apereo.cas.mfa.simple.web.flow;

import module java.base;
import org.apereo.cas.config.CasSurrogateAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasSurrogateAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.simple.BaseCasSimpleMultifactorAuthenticationTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.BaseMultifactorWebflowConfigurerTests;
import org.apereo.cas.web.support.WebUtils;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.execution.Action;
import static org.apereo.cas.web.flow.CasWebflowConstants.STATE_ID_LOAD_SURROGATES_ACTION;
import static org.apereo.cas.web.flow.CasWebflowConstants.TRANSITION_ID_SUCCESS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasSimpleMultifactorWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowMfaConfig")
@ExtendWith(CasTestExtension.class)
class CasSimpleMultifactorWebflowConfigurerTests {

    @SpringBootTest(classes = BaseCasSimpleMultifactorAuthenticationTests.SharedTestConfiguration.class,
        properties = {
            "cas.authn.mfa.simple.trusted-device-enabled=true",
            "cas.authn.mfa.simple.mail.accepted-email-pattern=.+",
            "cas.authn.mfa.trusted.core.device-registration-enabled=true"
        })
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @Getter
    @Nested
    class DefaultTests extends BaseMultifactorWebflowConfigurerTests {
        @Autowired
        @Qualifier("mfaSimpleAuthenticatorFlowRegistry")
        private FlowDefinitionRegistry multifactorFlowDefinitionRegistry;

        @Override
        protected String getMultifactorEventId() {
            return casProperties.getAuthn().getMfa().getSimple().getId();
        }
    }

    @SpringBootTest(classes = {
        CasSurrogateAuthenticationAutoConfiguration.class,
        CasSurrogateAuthenticationWebflowAutoConfiguration.class,
        BaseCasSimpleMultifactorAuthenticationTests.SharedTestConfiguration.class
    })
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @Getter
    @Nested
    class SurrogateTests extends BaseMultifactorWebflowConfigurerTests {
        @Autowired
        @Qualifier("mfaSimpleAuthenticatorFlowRegistry")
        private FlowDefinitionRegistry multifactorFlowDefinitionRegistry;

        @Override
        protected String getMultifactorEventId() {
            return casProperties.getAuthn().getMfa().getSimple().getId();
        }

        @Test
        void verifySurrogateOperation() {
            val flow = (Flow) flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
            var state = (TransitionableState) flow.getState(getMultifactorEventId());
            assertEquals(STATE_ID_LOAD_SURROGATES_ACTION, state.getTransition(TRANSITION_ID_SUCCESS).getTargetStateId());
        }
    }

    @SpringBootTest(classes = BaseCasSimpleMultifactorAuthenticationTests.SharedTestConfiguration.class,
        properties = {
            "cas.authn.mfa.simple.mail.accepted-email-pattern=.+@example.org",
            "CasFeatureModule.AccountManagement.enabled=true"
        })
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @Getter
    @Nested
    class AccountProfileTests extends BaseMultifactorWebflowConfigurerTests {
        @Autowired
        @Qualifier("mfaSimpleAuthenticatorFlowRegistry")
        private FlowDefinitionRegistry multifactorFlowDefinitionRegistry;

        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_SIMPLE_MFA_CONTACT_METHODS)
        private Action simpleMfaAccountProfileContactMethodsAction;

        @Override
        protected String getMultifactorEventId() {
            return casProperties.getAuthn().getMfa().getSimple().getId();
        }

        @Test
        void verifyAccountProfile() throws Throwable {
            val flow = (Flow) flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_ACCOUNT);
            val accountView = (ViewState) flow.getState(CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW);
            assertNotNull(accountView.getTransition(CasSimpleMultifactorAccountProfileWebflowConfigurer.TRANSITION_ID_REGISTER_EMAIL));

            val context = MockRequestContext.create(applicationContext);
            val principal = RegisteredServiceTestUtils.getPrincipal("casuser", Map.<String, List<Object>>of(
                "mail", List.of("casuser@example.org"),
                "email", List.of("casuser@example.org", "cas@example.org"),
                "phone", List.of("+1-555-0100")));
            WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(principal), context);
            assertNull(simpleMfaAccountProfileContactMethodsAction.execute(context));
            val methods = context.getFlowScope().get(CasSimpleMultifactorAccountProfileContactMethodsAction.FLOW_SCOPE_ATTR_CONTACT_METHODS, List.class);
            assertEquals(List.of(
                Map.of("type", "email", "value", "casuser@example.org"),
                Map.of("type", "email", "value", "cas@example.org"),
                Map.of("type", "phone", "value", "+1-555-0100")), methods);
            assertEquals(Boolean.TRUE, context.getFlowScope().get(
                CasSimpleMultifactorAccountProfileContactMethodsAction.FLOW_SCOPE_ATTR_EMAIL_REGISTRATION_ENABLED));
        }
    }
}
