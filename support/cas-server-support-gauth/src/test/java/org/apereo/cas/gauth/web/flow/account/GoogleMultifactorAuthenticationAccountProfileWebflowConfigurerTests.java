package org.apereo.cas.gauth.web.flow.account;

import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GoogleMultifactorAuthenticationAccountProfileWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("WebflowMfaConfig")
@Getter
@Import(BaseGoogleAuthenticatorTests.SharedTestConfiguration.class)
@TestPropertySource(properties = "CasFeatureModule.AccountManagement.enabled=true")
class GoogleMultifactorAuthenticationAccountProfileWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
    protected FlowDefinitionRegistry flowDefinitionRegistry;

    @Test
    void verifyOperation() throws Throwable {
        val flow = (Flow) flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_ACCOUNT);
        val accountView = (ViewState) flow.getState(CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW);
        assertEquals(4, accountView.getRenderActionList().size());
        assertTrue(accountView.getRenderActionList().get(0).toString().contains(CasWebflowConstants.ACTION_ID_PREPARE_ACCOUNT_PROFILE));
        assertNotNull(flow.getState(CasWebflowConstants.STATE_ID_VIEW_REGISTRATION));
        assertNotNull(flow.getState(CasWebflowConstants.STATE_ID_SAVE_REGISTRATION));
        assertNotNull(flow.getState(CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_GOOGLE_REGISTRATION_FINALIZED));

    }
}

