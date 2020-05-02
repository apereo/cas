package org.apereo.cas.ws.idp.web.flow;

import org.apereo.cas.config.CasWsSecurityTokenTicketCatalogConfiguration;
import org.apereo.cas.config.CasWsSecurityTokenTicketComponentSerializationConfiguration;
import org.apereo.cas.config.CoreWsSecurityIdentityProviderConfiguration;
import org.apereo.cas.config.CoreWsSecurityIdentityProviderWebflowConfiguration;
import org.apereo.cas.config.CoreWsSecuritySecurityTokenServiceConfiguration;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
    CoreWsSecurityIdentityProviderConfiguration.class,
    CoreWsSecurityIdentityProviderWebflowConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class
})
@TestPropertySource(properties = {
    "cas.authn.wsfedIdp.idp.realm=urn:org:apereo:cas:ws:idp:realm-CAS",
    "cas.authn.wsfedIdp.idp.realmName=CAS",
    
    "cas.authn.wsfedIdp.sts.signingKeystoreFile=classpath:ststrust.jks",
    "cas.authn.wsfedIdp.sts.signingKeystorePassword=storepass",

    "cas.authn.wsfedIdp.sts.encryptionKeystoreFile=classpath:stsencrypt.jks",
    "cas.authn.wsfedIdp.sts.encryptionKeystorePassword=storepass",

    "cas.authn.wsfedIdp.sts.subjectNameIdFormat=unspecified",
    "cas.authn.wsfedIdp.sts.encryptTokens=true",

    "cas.authn.wsfedIdp.sts.realm.keystoreFile=classpath:stsrealm_a.jks",
    "cas.authn.wsfedIdp.sts.realm.keystorePassword=storepass",
    "cas.authn.wsfedIdp.sts.realm.keystoreAlias=realma",
    "cas.authn.wsfedIdp.sts.realm.keyPassword=realma",
    "cas.authn.wsfedIdp.sts.realm.issuer=CAS"
})
@Tag("Webflow")
public class WSFederationIdentityProviderWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    public void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
    }
}
