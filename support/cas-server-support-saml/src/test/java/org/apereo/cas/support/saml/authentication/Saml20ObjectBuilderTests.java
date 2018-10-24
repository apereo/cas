package org.apereo.cas.support.saml.authentication;

import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.util.NonInflatingSaml20ObjectBuilder;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

/**
 * Tests the {@link AbstractSaml20ObjectBuilder}.
 *
 * @author Jerome Leleu
 * @since 5.2.9
 */
public class Saml20ObjectBuilderTests extends AbstractOpenSamlTests {

    private static final String BASE64_SAML_AUTHN_REQUEST = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48c2FtbDJwOkF1dG"
            + "huUmVxdWVzdCB4bWxuczpzYW1sMnA9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpwcm90b2NvbCIgQXNzZXJ0aW9uQ29uc3VtZXJTZXJ2aWN"
            + "lVVJMPSJodHRwOi8vbG9jYWxob3N0OjgwODEvY2FsbGJhY2s/Y2xpZW50X25hbWU9U0FNTDJDbGllbnQiIEZvcmNlQXV0aG49ImZhbHNlIiBJc3N1"
            + "ZUluc3RhbnQ9IjIwMTgtMTAtMDVUMTQ6NTI6NDcuMDg0WiIgUHJvdG9jb2xCaW5kaW5nPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YmluZ"
            + "GluZ3M6SFRUUC1QT1NUIiBWZXJzaW9uPSIyLjAiPjxzYW1sMjpJc3N1ZXIgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMD"
            + "phc3NlcnRpb24iPmh0dHA6Ly9sb2NhbGhvc3Q6ODA4MS9jYWxsYmFjazwvc2FtbDI6SXNzdWVyPjxzYW1sMnA6TmFtZUlEUG9saWN5IEFsbG93Q3J"
            + "lYXRlPSJ0cnVlIi8+PC9zYW1sMnA6QXV0aG5SZXF1ZXN0Pg==";

    private static final String SAML_AUTHN_REQUEST = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><saml2p:AuthnRequest "
            + "xmlns:saml2p=\"urn:oasis:names:tc:SAML:2.0:protocol\" AssertionConsumerServiceURL=\"http://localhost:8081/callback"
            + "?client_name=SAML2Client\" ForceAuthn=\"false\" IssueInstant=\"2018-10-05T14:52:47.084Z\" "
            + "ProtocolBinding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" Version=\"2.0\"><saml2:Issuer "
            + "xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion\">http://localhost:8081/callback</saml2:Issuer><saml2p:NameIDPolicy "
            + "AllowCreate=\"true\"/></saml2p:AuthnRequest>";

    @Test
    public void decodeNonInflatedSamlAuthnRequest() {
        val builder = new NonInflatingSaml20ObjectBuilder(this.configBean);
        val decoded = builder.decodeSamlAuthnRequest(BASE64_SAML_AUTHN_REQUEST);
        assertEquals(SAML_AUTHN_REQUEST, decoded);
    }
}
