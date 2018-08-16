package org.apereo.cas.support.x509.rest;

import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.authentication.Credential;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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
public class X509RestMultipartBodyCredentialFactoryTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private X509RestMultipartBodyCredentialFactory factory = new X509RestMultipartBodyCredentialFactory();

    @Test
    public void createX509Credential() throws IOException {
        final MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        final Scanner scan = new Scanner(new ClassPathResource("ldap-crl.crt").getFile(), StandardCharsets.UTF_8.name());
        final String certStr = scan.useDelimiter("\\Z").next();
        scan.close();
        requestBody.add("cert", certStr);

        final Credential cred = factory.fromRequest(null, requestBody).iterator().next();
        assertTrue(cred instanceof X509CertificateCredential);
    }

    @Test
    public void createDefaultCredential() {
        final MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("username", "name");
        requestBody.add("password", "passwd");
        final List<Credential> cred = factory.fromRequest(null, requestBody);
        assertTrue(cred.isEmpty());
    }
}
