package org.apereo.cas.ws.idp.web.flow;

import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreSamlAutoConfiguration;
import org.apereo.cas.config.CasWsSecurityIdentityProviderAutoConfiguration;
import org.apereo.cas.config.CasWsSecuritySecurityTokenAutoConfiguration;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.Flow;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WSFederationIdentityProviderWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@ImportAutoConfiguration({
    CasCoreAutoConfiguration.class,
    CasCoreSamlAutoConfiguration.class,
    CasWsSecuritySecurityTokenAutoConfiguration.class,
    CasWsSecurityIdentityProviderAutoConfiguration.class
})
@TestPropertySource(properties = {
    "cas.authn.wsfed-idp.idp.realm=urn:org:apereo:cas:ws:idp:realm-CAS",
    "cas.authn.wsfed-idp.idp.realm-name=CAS",

    "cas.authn.wsfed-idp.sts.signing-keystore-file=classpath:ststrust.jks",
    "cas.authn.wsfed-idp.sts.signing-keystore-password=storepass",

    "cas.authn.wsfed-idp.sts.encryption-keystore-file=classpath:stsencrypt.jks",
    "cas.authn.wsfed-idp.sts.encryption-keystore-password=storepass",

    "cas.authn.wsfed-idp.sts.subject-name-id-format=unspecified",
    "cas.authn.wsfed-idp.sts.encrypt-tokens=true",

    "cas.authn.wsfed-idp.sts.realm.keystore-file=classpath:stsrealm_a.jks",
    "cas.authn.wsfed-idp.sts.realm.keystore-password=storepass",
    "cas.authn.wsfed-idp.sts.realm.keystore-alias=realma",
    "cas.authn.wsfed-idp.sts.realm.key-password=realma",
    "cas.authn.wsfed-idp.sts.realm.issuer=CAS"
})
@Tag("WebflowConfig")
class WSFederationIdentityProviderWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier("wsFederationProtocolEndpointConfigurer")
    private CasWebSecurityConfigurer wsFederationProtocolEndpointConfigurer;

    @Test
    void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        assertFalse(wsFederationProtocolEndpointConfigurer.getIgnoredEndpoints().isEmpty());
    }
}
