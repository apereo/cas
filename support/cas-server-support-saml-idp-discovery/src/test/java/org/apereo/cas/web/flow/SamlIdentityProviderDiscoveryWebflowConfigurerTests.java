package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasCoreSamlAutoConfiguration;
import org.apereo.cas.config.CasDelegatedAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasSamlIdentityProviderDiscoveryAutoConfiguration;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.webflow.engine.Flow;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdentityProviderDiscoveryWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@ImportAutoConfiguration({
    CasCoreSamlAutoConfiguration.class,
    CasDelegatedAuthenticationAutoConfiguration.class,
    CasSamlIdentityProviderDiscoveryAutoConfiguration.class
})
@Tag("SAML2Web")
class SamlIdentityProviderDiscoveryWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
    }
}
