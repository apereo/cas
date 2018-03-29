package org.apereo.cas.support.saml.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.util.CompressionUtils;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.DeflaterOutputStream;

import static org.junit.Assert.*;

/**
 * Utility class to ensure authentication requests are properly encoded and decoded.
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
public class SamlAuthenticationRequestTests extends AbstractOpenSamlTests {

    @Test
    public void ensureDeflation() throws Exception {
        final var deflator = CompressionUtils.deflate(SAML_REQUEST);
        final var deflatorStream = deflateViaStream(SAML_REQUEST);
        assertEquals(deflatorStream, deflator);
    }

    private static String deflateViaStream(final String samlRequest) throws IOException {
        final var xmlBytes = samlRequest.getBytes(StandardCharsets.UTF_8);
        final var byteOutputStream = new ByteArrayOutputStream();
        final var deflaterOutputStream = new DeflaterOutputStream(
                byteOutputStream);
        deflaterOutputStream.write(xmlBytes, 0, xmlBytes.length);
        deflaterOutputStream.close();
        return EncodingUtils.encodeBase64(byteOutputStream.toByteArray());
    }
}
