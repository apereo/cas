package org.apereo.cas.adaptors.x509.authentication.principal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.cas.authentication.CredentialMetaData;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.security.cert.X509Certificate;

import static org.junit.Assert.assertEquals;

/**
 * @author Scott Battaglia
 * @since 3.0.
 */
public class X509CertificateCredentialTest {

    private static final File JSON_FILE = new File("x509CertificateCredential.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifySerializeAX509CertificateCredentialToJson() throws IOException {
        final X509Certificate certificate = new AbstractX509CertificateTests.CasX509Certificate(true);
        final X509CertificateCredential credentialWritten = new X509CertificateCredential(new X509Certificate[]{certificate});

        MAPPER.writeValue(JSON_FILE, credentialWritten);

        final CredentialMetaData credentialRead = MAPPER.readValue(JSON_FILE, X509CertificateCredential.class);

        assertEquals(credentialWritten, credentialRead);
    }
}
