package org.apereo.cas.ws.idp.authentication;

import org.apereo.cas.BaseCoreWsSecurityIdentityProviderConfigurationTests;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ws.idp.WSFederationConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WSFederationAuthenticationServiceSelectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseCoreWsSecurityIdentityProviderConfigurationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.wsfed-idp.idp.realm=urn:org:apereo:cas:ws:idp:realm-CAS",
        "cas.authn.wsfed-idp.idp.realm-name=CAS",

        "cas.authn.wsfed-idp.sts.signing-keystore-file=classpath:ststrust.jks",
        "cas.authn.wsfed-idp.sts.signing-keystore-password=storepass",

        "cas.authn.wsfed-idp.sts.encryption-keystore-file=classpath:stsencrypt.jks",
        "cas.authn.wsfed-idp.sts.encryption-keystore-password=storepass",

        "cas.authn.wsfed-idp.sts.subject-name-id-format=unspecified",
        "cas.authn.wsfed-idp.sts.encryptTokens=true",

        "cas.authn.wsfed-idp.sts.realm.keystore-file=stsrealm_a.jks",
        "cas.authn.wsfed-idp.sts.realm.keystore-password=storepass",
        "cas.authn.wsfed-idp.sts.realm.keystore-alias=realma",
        "cas.authn.wsfed-idp.sts.realm.key-password=realma",
        "cas.authn.wsfed-idp.sts.realm.issuer=CAS"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("WSFederation")
public class WSFederationAuthenticationServiceSelectionStrategyTests {
    @Autowired
    @Qualifier("wsFederationAuthenticationServiceSelectionStrategy")
    private AuthenticationServiceSelectionStrategy wsFederationAuthenticationServiceSelectionStrategy;

    @Test
    public void verifySupports() {
        assertFalse(wsFederationAuthenticationServiceSelectionStrategy.supports(null));

        val service = RegisteredServiceTestUtils.getService("https://cas.com");
        assertFalse(wsFederationAuthenticationServiceSelectionStrategy.supports(service));

        val service1 = RegisteredServiceTestUtils.getService("https://cas.com?" + WSFederationConstants.WREPLY
            + "=wreply&" + WSFederationConstants.WTREALM + "=realm");
        assertTrue(wsFederationAuthenticationServiceSelectionStrategy.supports(service1));

        val service2 = RegisteredServiceTestUtils.getService("https://cas.com?" + WSFederationConstants.WREPLY + "=wreply");
        assertFalse(wsFederationAuthenticationServiceSelectionStrategy.supports(service2));

    }

}
