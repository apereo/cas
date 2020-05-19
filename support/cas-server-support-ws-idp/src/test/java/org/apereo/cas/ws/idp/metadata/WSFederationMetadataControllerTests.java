package org.apereo.cas.ws.idp.metadata;

import org.apereo.cas.BaseCoreWsSecurityIdentityProviderConfigurationTests;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WSFederationMetadataControllerTests}.
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

    "cas.authn.wsfedIdp.sts.realm.keystoreFile=stsrealm_a.jks",
    "cas.authn.wsfedIdp.sts.realm.keystorePassword=storepass",
    "cas.authn.wsfedIdp.sts.realm.keystoreAlias=realma",
    "cas.authn.wsfedIdp.sts.realm.keyPassword=realma",
    "cas.authn.wsfedIdp.sts.realm.issuer=CAS"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Simple")
public class WSFederationMetadataControllerTests {
    @Autowired
    @Qualifier("wsFederationMetadataController")
    private WSFederationMetadataController wsFederationMetadataController;

    @Test
    public void verifyOperation() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        wsFederationMetadataController.doGet(request, response);
        assertEquals(response.getStatus(), HttpStatus.SC_OK);
    }
}
