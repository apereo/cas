package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.adaptors.x509.authentication.CasX509Certificate;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.security.cert.X509Certificate;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.
 */
public class X509CertificateCredentialTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "x509CertificateCredential.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verifySerializeAX509CertificateCredentialToJson() throws IOException {
        MAPPER.findAndRegisterModules();
        val certificate = new CasX509Certificate(true);
        val credentialWritten = new X509CertificateCredential(new X509Certificate[]{certificate});

        MAPPER.writeValue(JSON_FILE, credentialWritten);
        val credentialRead = MAPPER.readValue(JSON_FILE, X509CertificateCredential.class);
        assertEquals(credentialWritten, credentialRead);
    }
}
