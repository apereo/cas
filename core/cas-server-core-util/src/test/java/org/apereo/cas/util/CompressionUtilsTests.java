package org.apereo.cas.util;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

/**
 * This is {@link CompressionUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CompressionUtilsTests {

    @Test
    public void verifyInflation() {
        final String source = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<samlp:AuthnRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" "
            + "ID=\"5545454455\" Version=\"2.0\" IssueInstant=\"Value\" "
            + "ProtocolBinding=\"urn:oasis:names.tc:SAML:2.0:bindings:HTTP-Redirect\" "
            + "ProviderName=\"https://localhost:8443/myRutgers\" AssertionConsumerServiceURL=\"https://localhost:8443/myRutgers\"/>";
        val deflated = CompressionUtils.deflate(source);
        val decoded = EncodingUtils.decodeBase64(deflated);
        val results = CompressionUtils.decodeByteArrayToString(decoded);
        assertEquals(source, results);
    }
    
    @Test
    public void verifyStringCompression() {
        val srcTxt =
            "lamEiLCJhZG1pbiI6dHJ1ZX0.|..03f329983b86f7d9a9f5fef85305880101d5e302afafa20154d094b229f757|eyJhbGciO"
                + "iJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzY290Y2guaW8iLCJleHAiOjEzMDA4MTkzODAsIm5hbWUiOiJDaHJpcyBTZXZpbG"
                + "iJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzY290Y2guaW8iLCJleHAiOjEzMDA4MTkzODAsIm5hbWUiOiJDaHJpcyBTZXZpbG"
                + "iJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzY290Y2guaW8iLCJleHAiOjEzMDA4MTkzODAsIm5hbWUiOiJDaHJpcyBTZXZpbG"
                + "iJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzY290Y2guaW8iLCJleHAiOjEzMDA4MTkzODAsIm5hbWUiOiJDaHJpcyBTZXZpbG"
                + "iJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzY290Y2guaW8iLCJleHAiOjEzMDA4MTkzODAsIm5hbWUiOiJDaHJpcyBTZXZpbG"
                + "iJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzY290Y2guaW8iLCJleHAiOjEzMDA4MTkzODAsIm5hbWUiOiJDaHJpcyBTZXZpbG"
                + "iJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzY290Y2guaW8iLCJleHAiOjEzMDA4MTkzODAsIm5hbWUiOiJDaHJpcyBTZXZpbG"
                + "iJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzY290Y2guaW8iLCJleHAiOjEzMDA4MTkzODAsIm5hbWUiOiJDaHJpcyBTZXZpbG"
                + "xlamEiLCJhZG1pbiI6dHJ1ZX0.03f329983b86f7d9a9f5fef85305880101d5e302afafa20154d094b229f757";

        val str = CompressionUtils.compress(srcTxt);
        assertNotNull(str);

        val originalStr = CompressionUtils.decompress(str);
        assertNotNull(originalStr);

        assertEquals(srcTxt, originalStr);
    }
}
