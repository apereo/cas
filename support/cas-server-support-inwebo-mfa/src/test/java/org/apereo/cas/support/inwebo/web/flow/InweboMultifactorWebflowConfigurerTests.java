package org.apereo.cas.support.inwebo.web.flow;

import module java.base;
import org.apereo.cas.support.inwebo.config.BaseInweboConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.flow.configurer.BaseMultifactorWebflowConfigurerTests;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;

/**
 * Tests for {@link InweboMultifactorWebflowConfigurer}.
 *
 * @author Hayden Sartoris
 * @since 6.4.0
 */
@Tag("WebflowMfaConfig")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseInweboConfiguration.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.inwebo.client-certificate.certificate.location=classpath:clientcert.p12",
        "cas.authn.mfa.inwebo.client-certificate.passphrase=password"
})
@Getter
class InweboMultifactorWebflowConfigurerTests extends BaseMultifactorWebflowConfigurerTests {
    @Autowired
    @Qualifier("inweboFlowRegistry")
    private FlowDefinitionRegistry multifactorFlowDefinitionRegistry;

    @Override
    protected String getMultifactorEventId() {
        return InweboMultifactorWebflowConfigurer.MFA_INWEBO_EVENT_ID;
    }
}
