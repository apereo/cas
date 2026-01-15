package org.apereo.cas.mfa;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.flow.configurer.BaseMultifactorWebflowConfigurerTests;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;

/**
 * This is {@link CasTwilioMultifactorWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Getter
@Tag("WebflowMfaConfig")
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@SpringBootTest(
    classes = BaseTwilioMultifactorTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.twilio.core.account-id=${#randomString8}",
        "cas.authn.mfa.twilio.core.token=${#randomString8}"
    })
class CasTwilioMultifactorWebflowConfigurerTests extends BaseMultifactorWebflowConfigurerTests {
    @Autowired
    @Qualifier("mfaTwilioAuthenticatorFlowRegistry")
    private FlowDefinitionRegistry multifactorFlowDefinitionRegistry;

    @Override
    protected String getMultifactorEventId() {
        return casProperties.getAuthn().getMfa().getTwilio().getId();
    }
}
