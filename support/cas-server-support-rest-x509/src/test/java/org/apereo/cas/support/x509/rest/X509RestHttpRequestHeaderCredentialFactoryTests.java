package org.apereo.cas.support.x509.rest;

import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.web.extractcert.RequestHeaderX509CertificateExtractor;
import org.apereo.cas.authentication.Credential;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.LinkedMultiValueMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link X509RestMultipartBodyCredentialFactory}.
 *
 * @author Dmytro Fedonin
 * @since 5.1.0
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class X509RestHttpRequestHeaderCredentialFactoryTests {
    private static final String HEADER = "ssl_client_cert";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private X509RestHttpRequestHeaderCredentialFactory factory = new X509RestHttpRequestHeaderCredentialFactory(new RequestHeaderX509CertificateExtractor(HEADER));

    @Test
    public void createX509Credential() throws IOException {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final LinkedMultiValueMap<String, String> requestBody = new LinkedMultiValueMap<String, String>();
        final Scanner scan = new Scanner(new ClassPathResource("ldap-crl.crt").getFile(), StandardCharsets.UTF_8.name());
        final String certStr = scan.useDelimiter("\\Z").next();
        scan.close();
        
        request.addHeader(HEADER, certStr);

        final Credential cred = factory.fromRequest(request, requestBody).iterator().next();
        assertTrue("cred is not right type: "+cred, cred instanceof X509CertificateCredential);
    }

    @Test
    public void createDefaultCredential() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final LinkedMultiValueMap<String, String> requestBody = new LinkedMultiValueMap<String, String>();
        requestBody.add("username", "name");
        requestBody.add("password", "passwd");
        final List<Credential> cred = factory.fromRequest(request, requestBody);
        assertTrue(cred.isEmpty());
    }
}
