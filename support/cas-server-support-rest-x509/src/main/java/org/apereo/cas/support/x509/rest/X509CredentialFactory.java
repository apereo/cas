package org.apereo.cas.support.x509.rest;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.util.crypto.CertUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.support.rest.factory.DefaultCredentialFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.X509Certificate;

/**
 * This is {@link X509CredentialFactory} that attempts to read the contents
 * of the request body under {@link #CERTIFICATE} parameter to locate and construct
 * X509 credentials. If the request body does not contain a certificate,
 * it will then fallback onto the default behavior of capturing credentials.
 *
 * @author Dmytro Fedonin
 * @since 5.1.0
 */
public class X509CredentialFactory extends DefaultCredentialFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(X509CredentialFactory.class);
    private static final String CERTIFICATE = "cert";

    @Override
    public Credential fromRequestBody(final MultiValueMap<String, String> requestBody) {
        final String cert = requestBody.getFirst(CERTIFICATE);
        LOGGER.debug("Certificate in the request body: [{}]", cert);
        if (StringUtils.isBlank(cert)) {
            return super.fromRequestBody(requestBody);
        }
        final InputStream is = new ByteArrayInputStream(cert.getBytes());
        final InputStreamSource iso = new InputStreamResource(is);
        final X509Certificate certificate = CertUtils.readCertificate(iso);
        final X509CertificateCredential credential = new X509CertificateCredential(new X509Certificate[]{certificate});
        credential.setCertificate(certificate);
        return credential;
    }
}
