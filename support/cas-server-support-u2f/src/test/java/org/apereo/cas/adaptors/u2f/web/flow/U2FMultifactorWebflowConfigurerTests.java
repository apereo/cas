package org.apereo.cas.adaptors.u2f.web.flow;

import org.apereo.cas.web.flow.configurer.BaseMultifactorWebflowConfigurerTests;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;

/**
 * This is {@link U2FMultifactorWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = {
    BaseU2FWebflowActionTests.U2FTestConfiguration.class,
    BaseU2FWebflowActionTests.SharedTestConfiguration.class
}, properties = {
    "cas.authn.mfa.u2f.core.trusted-device-enabled=true",
    "cas.authn.mfa.trusted.core.device-registration-enabled=true"
})
@Tag("WebflowMfaConfig")
@Getter
public class U2FMultifactorWebflowConfigurerTests extends BaseMultifactorWebflowConfigurerTests {
    @Autowired
    @Qualifier("u2fFlowRegistry")
    private FlowDefinitionRegistry multifactorFlowDefinitionRegistry;

    @Override
    protected String getMultifactorEventId() {
        return U2FMultifactorWebflowConfigurer.MFA_U2F_EVENT_ID;
    }
}
