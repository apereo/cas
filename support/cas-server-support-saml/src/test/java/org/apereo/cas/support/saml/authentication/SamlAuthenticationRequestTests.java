package org.apereo.cas.support.saml.authentication;

import module java.base;
import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.util.EncodingUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Utility class to ensure authentication requests are properly encoded and decoded.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("SAML")
class SamlAuthenticationRequestTests extends AbstractOpenSamlTests {

    private static String deflateViaStream(final String samlRequest) throws IOException {
        val xmlBytes = samlRequest.getBytes(StandardCharsets.UTF_8);
        val byteOutputStream = new ByteArrayOutputStream();
        val deflaterOutputStream = new DeflaterOutputStream(
            byteOutputStream);
        deflaterOutputStream.write(xmlBytes, 0, xmlBytes.length);
        deflaterOutputStream.close();
        return EncodingUtils.encodeBase64(byteOutputStream.toByteArray());
    }

    @Test
    void ensureDeflation() throws Exception {
        val deflator = CompressionUtils.deflate(SAML_REQUEST);
        val deflatorStream = deflateViaStream(SAML_REQUEST);
        assertEquals(deflatorStream, deflator);
    }
}
