package org.apereo.cas.ws.idp.web.flow;

import org.apereo.cas.config.CasWsSecurityTokenTicketCatalogConfiguration;
import org.apereo.cas.config.CasWsSecurityTokenTicketComponentSerializationConfiguration;
import org.apereo.cas.config.CoreWsSecurityIdentityProviderComponentSerializationConfiguration;
import org.apereo.cas.config.CoreWsSecurityIdentityProviderConfiguration;
import org.apereo.cas.config.CoreWsSecurityIdentityProviderWebflowConfiguration;
import org.apereo.cas.config.CoreWsSecuritySecurityTokenServiceConfiguration;
import org.apereo.cas.config.CoreWsSecuritySecurityTokenTicketConfiguration;
import org.apereo.cas.web.ProtocolEndpointWebSecurityConfigurer;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.Flow;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WSFederationIdentityProviderWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import({
    CasWsSecurityTokenTicketCatalogConfiguration.class,
    CasWsSecurityTokenTicketComponentSerializationConfiguration.class,
    CoreWsSecuritySecurityTokenServiceConfiguration.class,
    CoreWsSecuritySecurityTokenTicketConfiguration.class,
    CoreWsSecurityIdentityProviderConfiguration.class,
    CoreWsSecurityIdentityProviderWebflowConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class,
    CoreWsSecurityIdentityProviderComponentSerializationConfiguration.class
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
public class WSFederationIdentityProviderWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier("wsFederationProtocolEndpointConfigurer")
    private ProtocolEndpointWebSecurityConfigurer wsFederationProtocolEndpointConfigurer;
    
    @Test
    public void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        assertFalse(wsFederationProtocolEndpointConfigurer.getIgnoredEndpoints().isEmpty());
    }
}
