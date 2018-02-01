package org.apereo.cas.support.x509.rest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.rest.RestHttpRequestCredentialFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CertUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link X509RestHttpRequestCredentialFactory} that attempts to read the contents
 * of the request body under {@link #CERTIFICATE} parameter to locate and construct
 * X509 credentials. If the request body does not contain a certificate,
 * it will then fallback onto the default behavior of capturing credentials.
 *
 * @author Dmytro Fedonin
 * @since 5.1.0
 */
@Slf4j
public class X509RestHttpRequestCredentialFactory implements RestHttpRequestCredentialFactory {

    private static final String CERTIFICATE = "cert";

    @Override
    public List<Credential> fromRequestBody(final MultiValueMap<String, String> requestBody) {
        final String cert = requestBody.getFirst(CERTIFICATE);
        LOGGER.debug("Certificate in the request body: [{}]", cert);
        if (StringUtils.isBlank(cert)) {
            return new ArrayList<>(0);
        }
        final InputStream is = new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8));
        final InputStreamSource iso = new InputStreamResource(is);
        final X509Certificate certificate = CertUtils.readCertificate(iso);
        final X509CertificateCredential credential = new X509CertificateCredential(new X509Certificate[]{certificate});
        credential.setCertificate(certificate);
        return CollectionUtils.wrap(credential);
    }
}
