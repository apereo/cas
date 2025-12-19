package org.apereo.cas.gauth.web.flow;

import module java.base;
import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
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
 * This is {@link GoogleAuthenticatorMultifactorWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseGoogleAuthenticatorTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.gauth.json.location=classpath:/repository.json",
        "cas.authn.mfa.gauth.core.trusted-device-enabled=true",
        "cas.authn.mfa.trusted.core.device-registration-enabled=true"
    })
@Tag("WebflowMfaConfig")
@ExtendWith(CasTestExtension.class)
@Getter
class GoogleAuthenticatorMultifactorWebflowConfigurerTests extends BaseMultifactorWebflowConfigurerTests {
    @Autowired
    @Qualifier("googleAuthenticatorFlowRegistry")
    private FlowDefinitionRegistry multifactorFlowDefinitionRegistry;
    
    @Override
    protected String getMultifactorEventId() {
        return casProperties.getAuthn().getMfa().getGauth().getId();
    }

}

