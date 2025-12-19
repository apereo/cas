package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreSamlAutoConfiguration;
import org.apereo.cas.config.CasWsFederationAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasWsFederationAuthenticationWebflowAutoConfiguration;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
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
@ImportAutoConfiguration({
    CasCoreSamlAutoConfiguration.class,
    CasWsFederationAuthenticationAutoConfiguration.class,
    CasWsFederationAuthenticationWebflowAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class
})
@TestPropertySource(properties = {
    "cas.authn.wsfed[0].identity-provider-url=https://example.org/adfs/ls/",
    "cas.authn.wsfed[0].identity-provider-identifier=https://example.org/adfs/services/trust",
    "cas.authn.wsfed[0].relying-party-identifier=urn:cas:example",
    "cas.authn.wsfed[0].signing-certificate-resources=classpath:adfs-signing.crt",
    "cas.authn.wsfed[0].identity-attribute=upn"
})
@Tag("WebflowConfig")
class WsFederationWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        val state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_WS_FEDERATION_START);
        assertNotNull(state);
    }
}
