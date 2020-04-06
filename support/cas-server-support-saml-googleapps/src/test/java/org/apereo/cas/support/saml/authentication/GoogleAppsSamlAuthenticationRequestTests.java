package org.apereo.cas.support.saml.authentication;

import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.saml.config.SamlGoogleAppsConfiguration;
import org.apereo.cas.support.saml.util.GoogleSaml20ObjectBuilder;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GoogleAppsSamlAuthenticationRequestTests}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 * @deprecated Since 6.2
 */
@SpringBootTest(classes = {
    SamlGoogleAppsConfiguration.class,
    AbstractOpenSamlTests.SharedTestConfiguration.class
})
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.server.name=http://localhost:8080",
    "cas.server.prefix=${server.name}/cas",
    "cas.samlCore.issuer=localhost",
    "cas.samlCore.skewAllowance=200",
    "cas.samlCore.ticketidSaml2=false",
    "cas.googleApps.keyAlgorithm=DSA",
    "cas.googleApps.publicKeyLocation=classpath:DSAPublicKey01.key",
    "cas.googleApps.privateKeyLocation=classpath:DSAPrivateKey01.key"
})
@Deprecated(since = "6.2.0")
public class GoogleAppsSamlAuthenticationRequestTests extends AbstractOpenSamlTests {
    @Autowired
    private ApplicationContextProvider applicationContextProvider;

    @BeforeEach
    public void init() {
        this.applicationContextProvider.setApplicationContext(this.applicationContext);
    }

    @Test
    public void ensureInflation() {
        val deflator = CompressionUtils.deflate(SAML_REQUEST);
        val builder = new GoogleSaml20ObjectBuilder(configBean);
        val msg = builder.decodeSamlAuthnRequest(deflator);
        assertEquals(SAML_REQUEST, msg);
    }

}
