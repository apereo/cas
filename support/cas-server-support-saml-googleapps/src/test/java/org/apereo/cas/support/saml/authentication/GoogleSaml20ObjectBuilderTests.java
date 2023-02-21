package org.apereo.cas.support.saml.authentication;

import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.config.SamlGoogleAppsConfiguration;
import org.apereo.cas.support.saml.util.GoogleSaml20ObjectBuilder;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GoogleSaml20ObjectBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 * @deprecated Since 6.2
 */
@SpringBootTest(classes = {
    SamlGoogleAppsConfiguration.class,
    AbstractOpenSamlTests.SharedTestConfiguration.class
})
@Tag("SAML2")
@TestPropertySource(properties = {
    "spring.main.allow-bean-definition-overriding=true",

    "cas.server.name=http://localhost:8080",
    "cas.server.prefix=${server.name}/cas",

    "cas.saml-core.issuer=localhost",
    "cas.saml-core.skew-allowance=200",
    "cas.saml-core.ticketid-saml2=false",

    "cas.google-apps.key-algorithm=DSA",
    "cas.google-apps.public-key-location=classpath:DSAPublicKey01.key",
    "cas.google-apps.private-key-location=classpath:DSAPrivateKey01.key"
})
@Deprecated(since = "6.2.0")
public class GoogleSaml20ObjectBuilderTests extends AbstractOpenSamlTests {

    private static final String BASE64_SAML_AUTHN_REQUEST = "fVJNT+MwEL2vtP/B8j1JEy6V1QR1QWgrwW5EA4e9uc4kMbU9weO0u/9+3RQEHOD6/OZ9jGd1+dcadgBPGl3J83TBGTiFrXZ9yR"
        + "+am2TJL6vv31YkrRnFegqDu4fnCSiwOOlIzA8ln7wTKEmTcNICiaDEdn13K4p0IUaPARUazjbXJd"
        + "/jOD7tLdhWyWHsu171YAanYNT73ZPaaY0jou44e3yNVZxibYgm2DgK0oUILfJlkhdJftHkS3FRiGL5h7P6xemHducGX8XanUkkfjZNndS/t80scNAt+F+RXfIesTeQKrQn"
        + "+1oS6UOEO2kIOFsTgQ8x4BU6miz4LfiDVvBwf1vyIYSRRJYdj8f0TSaTWR/i7lKrpdU4pNBOmVTEq3nBYu7o32326wbyNQGv3jxW2Tup6uXjTn021zUarf6xtTF4vPIgQywT"
        + "/BS73KC3Mnzulqf5jOg26WaqmByNoHSnoeUsq86uHy8k3s1/";
    
    @Test
    public void decodeNonInflatedSamlAuthnRequest() {
        val builder = new GoogleSaml20ObjectBuilder(this.configBean);
        val decoded = builder.decodeSamlAuthnRequest(BASE64_SAML_AUTHN_REQUEST);
        val authnRequest = SamlUtils.transformSamlObject(this.configBean, decoded, AuthnRequest.class);
        assertNotNull(authnRequest);
    }
}
