package org.apereo.cas.support.x509.rest;

import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.web.extractcert.RequestHeaderX509CertificateExtractor;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.LinkedMultiValueMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link X509RestMultipartBodyCredentialFactory}.
 *
 * @author Dmytro Fedonin
 * @since 5.1.0
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class X509RestHttpRequestHeaderCredentialFactoryTests {
    private static final String HEADER = "ssl_client_cert";

    private final X509RestHttpRequestHeaderCredentialFactory factory = new X509RestHttpRequestHeaderCredentialFactory(new RequestHeaderX509CertificateExtractor(HEADER));

    @Test
    public void createX509Credential() throws IOException {
        val request = new MockHttpServletRequest();
        try (val scan = new Scanner(new ClassPathResource("ldap-crl.crt").getFile(), StandardCharsets.UTF_8.name())) {
            val certStr = scan.useDelimiter("\\Z").next();
            request.addHeader(HEADER, certStr);

            val cred = factory.fromRequest(request, null).iterator().next();
            assertTrue(cred instanceof X509CertificateCredential);
        }
    }

    @Test
    public void createDefaultCredential() {
        val request = new MockHttpServletRequest();
        val requestBody = new LinkedMultiValueMap<String, String>();
        requestBody.add("username", "name");
        requestBody.add("password", "passwd");
        val cred = factory.fromRequest(request, requestBody);
        assertTrue(cred.isEmpty());
    }
}
