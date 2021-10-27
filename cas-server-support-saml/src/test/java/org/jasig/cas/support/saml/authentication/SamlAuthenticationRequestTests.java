package org.jasig.cas.support.saml.authentication;

import org.jasig.cas.support.saml.AbstractOpenSamlTests;
import org.jasig.cas.util.CompressionUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;

import static org.junit.Assert.*;

/**
 * Utility class to ensure authentication requests are properly encoded and decoded.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class SamlAuthenticationRequestTests extends AbstractOpenSamlTests {

    @Test
    public void ensureDeflation() throws Exception {
        final String deflator = CompressionUtils.deflate(SAML_REQUEST);
        final String deflatorStream = deflateViaStream(SAML_REQUEST);
        assertEquals(deflatorStream, deflator);
    }

    private String deflateViaStream(final String samlRequest) throws IOException {
        final byte[] xmlBytes = samlRequest.getBytes("UTF-8");
        final ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        final DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(
                byteOutputStream);
        deflaterOutputStream.write(xmlBytes, 0, xmlBytes.length);
        deflaterOutputStream.close();
        return CompressionUtils.encodeBase64(byteOutputStream.toByteArray());
    }
}
