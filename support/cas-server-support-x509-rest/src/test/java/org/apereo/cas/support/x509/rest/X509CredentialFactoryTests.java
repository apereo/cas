package org.apereo.cas.support.x509.rest;

import static org.junit.Assert.assertTrue;

import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.support.rest.BadRequestException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.Scanner;

/**
 * Unit tests for {@link X509CredentialFactory}.
 *
 * @author Dmytro Fedonin
 */
@RunWith(MockitoJUnitRunner.class)
public class X509CredentialFactoryTests {

    @InjectMocks
    private X509CredentialFactory factory;

    @Test
    public void createX509Credential() throws IOException {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        Scanner scan = new Scanner(new ClassPathResource("ldap-crl.crt").getFile());
        String certStr = scan.useDelimiter("\\Z").next();
        scan.close();
        requestBody.add("cert", certStr);

        Credential cred = factory.fromRequestBody(requestBody);
        assertTrue(cred instanceof X509CertificateCredential);
    }

    @Test
    public void createDefaultCredential() {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("username", "name");
        requestBody.add("password", "passwd");

        Credential cred = factory.fromRequestBody(requestBody);
        assertTrue(cred instanceof UsernamePasswordCredential);
    }

    @Test(expected = BadRequestException.class)
    public void createInvalidCredential() {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("username", "name");

        Credential cred = factory.fromRequestBody(requestBody);
    }
}
