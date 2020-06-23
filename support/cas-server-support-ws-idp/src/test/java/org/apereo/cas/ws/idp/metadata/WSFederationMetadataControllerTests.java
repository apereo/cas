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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WSFederationMetadataControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
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
