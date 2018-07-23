package org.apereo.cas.support.x509.rest;

import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;

import lombok.val;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.LinkedMultiValueMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.Assert.*;

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
        val requestBody = new LinkedMultiValueMap<String, String>();
        val scan = new Scanner(new ClassPathResource("ldap-crl.crt").getFile(), StandardCharsets.UTF_8.name());
        val certStr = scan.useDelimiter("\\Z").next();
        scan.close();
        requestBody.add("cert", certStr);

        val cred = factory.fromRequestBody(requestBody).iterator().next();
        assertTrue(cred instanceof X509CertificateCredential);
    }

    @Test
    public void createDefaultCredential() {
        val requestBody = new LinkedMultiValueMap<String, String>();
        requestBody.add("username", "name");
        requestBody.add("password", "passwd");
        val cred = factory.fromRequestBody(requestBody);
        assertTrue(cred.isEmpty());
    }
}
