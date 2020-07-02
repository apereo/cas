package org.apereo.cas.adaptors.radius.web.flow;

import org.apereo.cas.web.flow.configurer.BaseMultifactorWebflowConfigurerTests;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;

/**
 * This is {@link RadiusMultifactorWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseRadiusMultifactorAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.trusted.device-registration-enabled=true",

        "cas.authn.radius.client.shared-secret=NoSecret",
        "cas.authn.radius.client.inet-address=localhost,localguest",
        
        "cas.authn.mfa.radius.trusted-device-enabled=true",
        "cas.authn.mfa.radius.client.shared-secret=NoSecret",
        "cas.authn.mfa.radius.client.inet-address=localhost,localguest",

        "cas.webflow.crypto.encryption.key=qLhvLuaobvfzMmbo9U_bYA",
        "cas.webflow.crypto.signing.key=oZeAR5pEXsolruu4OQYsQKxf-FCvFzSsKlsVaKmfIl6pNzoPm6zPW94NRS1af7vT-0bb3DpPBeksvBXjloEsiA"
    })
@Tag("Radius")
@Getter
public class RadiusMultifactorWebflowConfigurerTests extends BaseMultifactorWebflowConfigurerTests {
    @Autowired
    @Qualifier("radiusFlowRegistry")
    private FlowDefinitionRegistry multifactorFlowDefinitionRegistry;

    @Override
    protected String getMultifactorEventId() {
        return RadiusMultifactorWebflowConfigurer.MFA_RADIUS_EVENT_ID;
    }
}

