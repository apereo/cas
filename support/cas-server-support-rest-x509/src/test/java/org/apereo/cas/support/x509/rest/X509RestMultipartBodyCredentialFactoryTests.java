package org.apereo.cas.support.x509.rest;

import module java.base;
import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import lombok.Cleanup;
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
class X509RestMultipartBodyCredentialFactoryTests {

    private final X509RestMultipartBodyCredentialFactory factory = new X509RestMultipartBodyCredentialFactory();

    @Test
    void emptyRequestBody() {
        val requestBody = new LinkedMultiValueMap<String, String>();
        val cred = factory.fromRequest(new MockHttpServletRequest(), requestBody);
        assertTrue(cred.isEmpty());
    }

    @Test
    void badCredential() {
        val requestBody = new LinkedMultiValueMap<String, String>();
        requestBody.add("cert", "bad-certificate");
        val cred = factory.fromRequest(new MockHttpServletRequest(), requestBody);
        assertTrue(cred.isEmpty());
    }

    @Test
    void createX509Credential() throws IOException {
        val requestBody = new LinkedMultiValueMap<String, String>();
        @Cleanup
        val scan = new Scanner(new ClassPathResource("ldap-crl.crt").getFile(), StandardCharsets.UTF_8);
        val certStr = scan.useDelimiter("\\Z").next();
        scan.close();
        requestBody.add("cert", certStr);
        val cred = factory.fromRequest(new MockHttpServletRequest(), requestBody).getFirst();
        assertInstanceOf(X509CertificateCredential.class, cred);
    }

    @Test
    void createDefaultCredential() {
        val requestBody = new LinkedMultiValueMap<String, String>();
        requestBody.add("username", "name");
        requestBody.add("password", "passwd");
        val cred = factory.fromRequest(new MockHttpServletRequest(), requestBody);
        assertTrue(cred.isEmpty());
    }
}
