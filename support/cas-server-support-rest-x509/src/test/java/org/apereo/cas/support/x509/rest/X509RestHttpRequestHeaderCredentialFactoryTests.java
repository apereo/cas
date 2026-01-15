package org.apereo.cas.support.x509.rest;

import module java.base;
import org.apereo.cas.adaptors.x509.authentication.RequestHeaderX509CertificateExtractor;
import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.LinkedMultiValueMap;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link X509RestMultipartBodyCredentialFactory}.
 *
 * @author Dmytro Fedonin
 * @since 5.1.0
 */
@ExtendWith(MockitoExtension.class)
@Tag("X509")
class X509RestHttpRequestHeaderCredentialFactoryTests {
    private static final String HEADER = "ssl_client_cert";

    private final X509RestHttpRequestHeaderCredentialFactory factory =
        new X509RestHttpRequestHeaderCredentialFactory(new RequestHeaderX509CertificateExtractor(HEADER));

    @Test
    void createX509Credential() throws IOException {
        val request = new MockHttpServletRequest();
        try (val scan = new Scanner(new ClassPathResource("ldap-crl.crt").getFile(), StandardCharsets.UTF_8)) {
            val certStr = scan.useDelimiter("\\Z").next();
            request.addHeader(HEADER, certStr);

            val cred = factory.fromRequest(request, null).getFirst();
            assertInstanceOf(X509CertificateCredential.class, cred);
        }
    }

    @Test
    void createDefaultCredential() {
        val request = new MockHttpServletRequest();
        val requestBody = new LinkedMultiValueMap<String, String>();
        requestBody.add("username", "name");
        requestBody.add("password", "passwd");
        val cred = factory.fromRequest(request, requestBody);
        assertTrue(cred.isEmpty());
    }
}
