package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.adaptors.x509.authentication.CasX509Certificate;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import java.io.File;
import java.security.cert.X509Certificate;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for {@link X509CertificateCredential}.
 * @author Scott Battaglia
 * @since 3.0.
 */
@Tag("X509")
class X509CertificateCredentialTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "x509CertificateCredential.json");
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifySerializeAX509CertificateCredentialToJson() {
        val certificate = new CasX509Certificate(true);
        val credentialWritten = new X509CertificateCredential(new X509Certificate[]{certificate});

        MAPPER.writeValue(JSON_FILE, credentialWritten);
        val credentialRead = MAPPER.readValue(JSON_FILE, X509CertificateCredential.class);
        assertEquals(credentialWritten, credentialRead);
    }
}
