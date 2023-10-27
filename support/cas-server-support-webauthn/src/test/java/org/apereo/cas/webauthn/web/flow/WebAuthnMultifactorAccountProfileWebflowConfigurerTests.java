package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.config.CasWebflowAccountProfileConfiguration;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WebAuthnMultifactorAccountProfileWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("WebflowMfaConfig")
@Import(CasWebflowAccountProfileConfiguration.class)
@SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class,
    properties = "CasFeatureModule.AccountManagement.enabled=true")
public class WebAuthnMultifactorAccountProfileWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier(CasWebflowConstants.BEAN_NAME_ACCOUNT_PROFILE_FLOW_DEFINITION_REGISTRY)
    protected FlowDefinitionRegistry accountFlowDefinitionRegistry;

    @Test
    void verifyOperation() throws Throwable {
        val flow = (Flow) accountFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_ACCOUNT);
        val accountView = (ViewState) flow.getState(CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW);
        assertEquals(4, accountView.getRenderActionList().size());
        assertTrue(accountView.getRenderActionList().get(0).toString().contains(CasWebflowConstants.ACTION_ID_PREPARE_ACCOUNT_PROFILE));
        val registrationState = (ViewState) flow.getState(CasWebflowConstants.STATE_ID_WEBAUTHN_VIEW_REGISTRATION);
        assertNotNull(registrationState);
        assertEquals(5, registrationState.getEntryActionList().size());
    }
}

