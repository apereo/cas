package org.apereo.cas.support.x509.rest;

import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.util.crypto.CertUtils;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.LinkedMultiValueMap;

import java.io.FileInputStream;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link X509RestTlsClientCertCredentialFactory}.
 *
 * @author Dmytro Fedonin
 * @author Stéphane Adenot
 * @since 6.0.0
 */
@Tag("X509")
public class X509RestTlsClientCertCredentialFactoryTests {
    private static final String REQUEST_ATTRIBUTE_X509_CERTIFICATE = "javax.servlet.request.X509Certificate";

    private final X509RestTlsClientCertCredentialFactory factory = new X509RestTlsClientCertCredentialFactory();

    @Test
    @SneakyThrows
    public void createX509Credential() {
        val request = new MockHttpServletRequest();

        try (val inStream = new FileInputStream(new ClassPathResource("ldap-crl.crt").getFile())) {
            val certs = new X509Certificate[]{CertUtils.readCertificate(inStream)};
            request.setAttribute(REQUEST_ATTRIBUTE_X509_CERTIFICATE, certs);

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
