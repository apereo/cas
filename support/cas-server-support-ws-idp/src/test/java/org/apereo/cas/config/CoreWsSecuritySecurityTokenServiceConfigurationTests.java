package org.apereo.cas.config;

import org.apereo.cas.BaseCoreWsSecurityIdentityProviderConfigurationTests;

import org.apache.cxf.sts.token.realm.RealmProperties;
import org.apache.cxf.ws.security.sts.provider.SecurityTokenServiceProvider;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CoreWsSecuritySecurityTokenServiceConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    BaseCoreWsSecurityIdentityProviderConfigurationTests.SharedTestConfiguration.class
}, properties = {
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
@Tag("Simple")
public class CoreWsSecuritySecurityTokenServiceConfigurationTests {
    @Autowired
    @Qualifier("cxfServlet")
    private ServletRegistrationBean cxfServlet;

    @Autowired
    @Qualifier("transportSTSProviderBean")
    private SecurityTokenServiceProvider transportSTSProviderBean;

    @Autowired
    @Qualifier("casRealm")
    private RealmProperties casRealm;

    @Test
    public void verifyOperation() {
        assertNotNull(cxfServlet);
        assertNotNull(transportSTSProviderBean);
        assertNotNull(casRealm);
    }

}
