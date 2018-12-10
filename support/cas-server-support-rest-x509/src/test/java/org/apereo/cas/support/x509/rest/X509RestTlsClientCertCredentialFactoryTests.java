package org.apereo.cas.support.x509.rest;

import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.util.crypto.CertUtils;

import lombok.val;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.LinkedMultiValueMap;

import java.io.FileInputStream;
import java.io.IOException;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link X509RestTlsClientCertCredentialFactory}.
 *
 * @author Dmytro Fedonin
 * @author sadt
 * @since 6.0.0
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class X509RestTlsClientCertCredentialFactoryTests {

    private static final String REQUEST_ATTRIBUTE_X509_CERTIFICATE = "javax.servlet.request.X509Certificate";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final X509RestTlsClientCertCredentialFactory factory = new X509RestTlsClientCertCredentialFactory();

    @Test
    public void createX509Credential() throws IOException, CertificateException {
        val request = new MockHttpServletRequest();

        try (val inStream = new FileInputStream(new ClassPathResource("ldap-crl.crt").getFile())) {
            X509Certificate[] certs = {CertUtils.readCertificate(inStream)};
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
