package org.apereo.cas.web.flow;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.duo.config.DuoSecurityAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.adaptors.duo.config.DuoSecurityConfiguration;
import org.apereo.cas.adaptors.duo.config.DuoSecurityMultifactorProviderBypassConfiguration;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderBean;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.SurrogateAuthenticationAuditConfiguration;
import org.apereo.cas.config.SurrogateAuthenticationConfiguration;
import org.apereo.cas.config.SurrogateAuthenticationWebflowConfiguration;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;

import static org.apereo.cas.web.flow.CasWebflowConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SurrogateWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowConfig")
public class SurrogateWebflowConfigurerTests {

    @Import({
        CasCoreMultifactorAuthenticationConfiguration.class,
        CasMultifactorAuthenticationWebflowConfiguration.class,
        SurrogateAuthenticationConfiguration.class,
        SurrogateAuthenticationAuditConfiguration.class,
        SurrogateAuthenticationWebflowConfiguration.class,
        BaseWebflowConfigurerTests.SharedTestConfiguration.class
    })
    public static class SharedTestConfiguration {
    }

    @TestConfiguration("DuoSecurityTestConfiguration")
    public static class DuoSecurityTestConfiguration {

        @Bean
        public MultifactorAuthenticationProviderBean
            <DuoSecurityMultifactorAuthenticationProvider, DuoSecurityMultifactorAuthenticationProperties> duoProviderBean() {
            val provider = mock(DuoSecurityMultifactorAuthenticationProvider.class);
            when(provider.getId()).thenReturn(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
            when(provider.getDuoAuthenticationService()).thenReturn(mock(DuoSecurityAuthenticationService.class));
            when(provider.matches(anyString())).thenReturn(true);
            val bean = mock(MultifactorAuthenticationProviderBean.class);
            when(bean.getProvider(anyString())).thenReturn(provider);
            return bean;
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @Import(SurrogateWebflowConfigurerTests.SharedTestConfiguration.class)
    public class DefaultTests extends BaseWebflowConfigurerTests {

        @Test
        public void verifyOperation() {
            assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
            val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
            assertNotNull(flow);
            var state = (TransitionableState) flow.getState(STATE_ID_LOAD_SURROGATES_ACTION);
            assertNotNull(state);
            state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_SELECT_SURROGATE);
            assertNotNull(state);
            state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_SURROGATE_VIEW);
            assertNotNull(state);
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @Import({
        DuoSecurityTestConfiguration.class,
        DuoSecurityConfiguration.class,
        DuoSecurityAuthenticationEventExecutionPlanConfiguration.class,
        DuoSecurityMultifactorProviderBypassConfiguration.class,
        SurrogateWebflowConfigurerTests.SharedTestConfiguration.class
    })
    @TestPropertySource(properties = {
        "cas.authn.mfa.duo[0].duo-secret-key=1234567890",
        "cas.authn.mfa.duo[0].duo-integration-key=QRSTUVWXYZ",
        "cas.authn.mfa.duo[0].duo-api-host=theapi.duosecurity.com"
    })
    public class DuoSecurityUniversalPromptTests extends BaseWebflowConfigurerTests {
        @Autowired
        @Qualifier("surrogateDuoSecurityMultifactorAuthenticationWebflowConfigurer")
        private CasWebflowConfigurer surrogateDuoSecurityMultifactorAuthenticationWebflowConfigurer;

        @Autowired
        @Qualifier("surrogateCasMultifactorWebflowCustomizer")
        private CasMultifactorWebflowCustomizer surrogateCasMultifactorWebflowCustomizer;

        @Test
        public void verifyOperation() {
            assertNotNull(surrogateDuoSecurityMultifactorAuthenticationWebflowConfigurer);
            assertNotNull(surrogateCasMultifactorWebflowCustomizer);
            val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
            assertNotNull(flow);
            var state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_DUO_UNIVERSAL_PROMPT_VALIDATE_LOGIN);
            assertEquals(STATE_ID_LOAD_SURROGATES_ACTION, state.getTransition(TRANSITION_ID_SUCCESS).getTargetStateId());

            val mappings = surrogateCasMultifactorWebflowCustomizer.getMultifactorWebflowAttributeMappings();
            assertTrue(mappings.contains(WebUtils.REQUEST_SURROGATE_ACCOUNT_ATTRIBUTE));
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @Import({
        DuoSecurityTestConfiguration.class,
        DuoSecurityConfiguration.class,
        DuoSecurityAuthenticationEventExecutionPlanConfiguration.class,
        DuoSecurityMultifactorProviderBypassConfiguration.class,
        SurrogateWebflowConfigurerTests.SharedTestConfiguration.class
    })
    @TestPropertySource(properties = {
        "cas.authn.mfa.duo[0].duo-secret-key=1234567890",
        "cas.authn.mfa.duo[0].application-key=my134nfd46m89",
        "cas.authn.mfa.duo[0].duo-integration-key=QRSTUVWXYZ",
        "cas.authn.mfa.duo[0].duo-api-host=theapi.duosecurity.com"
    })
    @Deprecated(since = "6.5.0", forRemoval = true)
    public class DuoSecurityWebSdkTests extends BaseWebflowConfigurerTests {
        @Test
        public void verifyOperation() {
            val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
            var state = (TransitionableState) flow.getState("mfa-duo");
            assertEquals(STATE_ID_LOAD_SURROGATES_ACTION, state.getTransition(TRANSITION_ID_SUCCESS).getTargetStateId());
        }
    }
}
