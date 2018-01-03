package org.apereo.cas.support.x509.rest;

import static org.junit.Assert.*;

import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.rest.BadRestRequestException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Unit tests for {@link X509RestHttpRequestCredentialFactory}.
 *
 * @author Dmytro Fedonin
 * @since 5.1.0
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class X509RestHttpRequestCredentialFactoryTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @InjectMocks
    private X509RestHttpRequestCredentialFactory factory;

    @Test
    public void createX509Credential() throws IOException {
        final MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        final Scanner scan = new Scanner(new ClassPathResource("ldap-crl.crt").getFile(), StandardCharsets.UTF_8.name());
        final String certStr = scan.useDelimiter("\\Z").next();
        scan.close();
        requestBody.add("cert", certStr);

        final Credential cred = factory.fromRequestBody(requestBody).iterator().next();
        assertTrue(cred instanceof X509CertificateCredential);
    }

    @Test
    public void createDefaultCredential() {
        final MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("username", "name");
        requestBody.add("password", "passwd");

        final Credential cred = factory.fromRequestBody(requestBody).iterator().next();
        assertTrue(cred instanceof UsernamePasswordCredential);
    }

    @Test
    public void createInvalidCredential() {
        final MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("username", "name");

        thrown.expect(BadRestRequestException.class);
        factory.fromRequestBody(requestBody);
        fail();
    }
}
