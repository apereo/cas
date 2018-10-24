package org.apereo.cas.support.saml.authentication;

import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.saml.config.SamlGoogleAppsConfiguration;
import org.apereo.cas.support.saml.util.GoogleSaml20ObjectBuilder;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.*;

/**
 * This is {@link GoogleSaml20ObjectBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Import(SamlGoogleAppsConfiguration.class)
@TestPropertySource(locations = "classpath:/gapps.properties")
public class GoogleSaml20ObjectBuilderTests extends AbstractOpenSamlTests {

    private static final String BASE64_SAML_AUTHN_REQUEST = "fVJNT+MwEL2vtP/B8j1JEy6V1QR1QWgrwW5EA4e9uc4kMbU9weO0u/9+3RQEHOD6/OZ9jGd1+dcadgBPGl3J83TBGTiFrXZ9yR"
        + "+am2TJL6vv31YkrRnFegqDu4fnCSiwOOlIzA8ln7wTKEmTcNICiaDEdn13K4p0IUaPARUazjbXJd"
        + "/jOD7tLdhWyWHsu171YAanYNT73ZPaaY0jou44e3yNVZxibYgm2DgK0oUILfJlkhdJftHkS3FRiGL5h7P6xemHducGX8XanUkkfjZNndS/t80scNAt+F+RXfIesTeQKrQn"
        + "+1oS6UOEO2kIOFsTgQ8x4BU6miz4LfiDVvBwf1vyIYSRRJYdj8f0TSaTWR/i7lKrpdU4pNBOmVTEq3nBYu7o32326wbyNQGv3jxW2Tup6uXjTn021zUarf6xtTF4vPIgQywT"
        + "/BS73KC3Mnzulqf5jOg26WaqmByNoHSnoeUsq86uHy8k3s1/";

    private static final String SAML_AUTHN_REQUEST = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
        + "<samlp:AuthnRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" ID=\"koppjkmemdcahpgfgcgelhncepikbjcbiiopooif\" Version=\"2.0\" "
        + "IssueInstant=\"2018-12-13T18:32:28Z\" ProtocolBinding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" ProviderName=\"google.com\" IsPassive=\"false\""
        + " AssertionConsumerServiceURL=\"https://www.google.com/a/gtest.miamioh.edu/acs\"><saml:Issuer xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">google"
        + ".com</saml:Issuer><samlp:NameIDPolicy AllowCreate=\"true\" Format=\"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified\" /></samlp:AuthnRequest>\r\n";

    @Test
    public void decodeNonInflatedSamlAuthnRequest() {
        val builder = new GoogleSaml20ObjectBuilder(this.configBean);
        val decoded = builder.decodeSamlAuthnRequest(BASE64_SAML_AUTHN_REQUEST);
        assertEquals(SAML_AUTHN_REQUEST, decoded);
    }
}
