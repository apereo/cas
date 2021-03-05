package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CoreSamlConfiguration;
import org.apereo.cas.support.wsfederation.config.WsFederationAuthenticationConfiguration;
import org.apereo.cas.support.wsfederation.config.support.authentication.WsFedAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.WsFederationAuthenticationWebflowConfiguration;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WsFederationWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import({
    CoreSamlConfiguration.class,
    WsFederationAuthenticationConfiguration.class,
    WsFedAuthenticationEventExecutionPlanConfiguration.class,
    WsFederationAuthenticationWebflowConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class
})
@TestPropertySource(properties = {
    "cas.authn.wsfed[0].identity-provider-url=https://example.org/adfs/ls/",
    "cas.authn.wsfed[0].identity-provider-identifier=https://example.org/adfs/services/trust",
    "cas.authn.wsfed[0].relying-party-identifier=urn:cas:example",
    "cas.authn.wsfed[0].signing-certificate-resources=classpath:adfs-signing.crt",
    "cas.authn.wsfed[0].identity-attribute=upn"
})
@Tag("WebflowConfig")
public class WsFederationWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    public void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        var state = (TransitionableState) flow.getState(WsFederationWebflowConfigurer.STATE_ID_WS_FEDERATION_ACTION);
        assertNotNull(state);
    }
}
