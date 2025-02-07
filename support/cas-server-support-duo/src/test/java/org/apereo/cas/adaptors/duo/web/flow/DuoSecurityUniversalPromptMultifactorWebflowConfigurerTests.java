package org.apereo.cas.adaptors.duo.web.flow;

import org.apereo.cas.adaptors.duo.BaseDuoSecurityTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.BaseMultifactorWebflowConfigurerTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DuoSecurityMultifactorWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseDuoSecurityTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.duo[0].duo-secret-key=Q2IU2i8BFNd6VYflZT8Evl6lF7oPlj3PM15BmRU7",
        "cas.authn.mfa.duo[0].duo-integration-key=DIOXVRZD2UMZ8XXMNFQ5",
        "cas.authn.mfa.duo[0].trusted-device-enabled=true",
        "cas.authn.mfa.duo[0].duo-api-host=theapi.duosecurity.com"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("DuoSecurity")
@ExtendWith(CasTestExtension.class)
class DuoSecurityUniversalPromptMultifactorWebflowConfigurerTests extends BaseMultifactorWebflowConfigurerTests {

    @Test
    void verifyUniversalPromptFlow() {
        val loginFlow = (Flow) flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(loginFlow.getState(CasWebflowConstants.STATE_ID_DUO_UNIVERSAL_PROMPT_VALIDATE_LOGIN));
        assertEquals(CasWebflowConstants.STATE_ID_DUO_UNIVERSAL_PROMPT_VALIDATE_LOGIN, loginFlow.getStartState().getId());

        val registry = getMultifactorFlowDefinitionRegistry();
        val flow = (Flow) registry.getFlowDefinition(getMultifactorEventId());
        val viewState = (ActionState) flow.getState(CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM_DUO);
        assertTrue(viewState.getActionList().get(0).toString()
            .contains(CasWebflowConstants.ACTION_ID_DUO_UNIVERSAL_PROMPT_PREPARE_LOGIN));
    }

    @Override
    protected FlowDefinitionRegistry getMultifactorFlowDefinitionRegistry() {
        return applicationContext.getBean(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER, FlowDefinitionRegistry.class);
    }

    @Override
    protected String getMultifactorEventId() {
        return DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER;
    }

}

