package org.apereo.cas.support.x509.rest;

import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.adaptors.x509.util.CertUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.support.rest.DefaultCredentialFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.X509Certificate;

/**
 * This is {@link X509CredentialFactory}.
 *
 * @author Dmytro Fedonin
 */
public class X509CredentialFactory extends DefaultCredentialFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(X509CredentialFactory.class);
    private static final String CERTIFICATE = "cert";

    @Override
    public Credential fromRequestBody(final MultiValueMap<String, String> requestBody) {
        final String cert = requestBody.getFirst(CERTIFICATE);
        LOGGER.trace("cert: {}", cert);
        if (cert == null) {
            LOGGER.debug("cert is null fallback to username/passwd");
           return super.fromRequestBody(requestBody);
        }
        InputStream is = new ByteArrayInputStream(cert.getBytes());
        InputStreamSource iso = new InputStreamResource(is);
        X509Certificate certificate = CertUtils.readCertificate(iso);
        X509CertificateCredential credential = new X509CertificateCredential(new X509Certificate[] { certificate });
        credential.setCertificate(certificate);

        return credential;
    }
}
