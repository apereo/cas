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
@Tag("WSFederation")
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
