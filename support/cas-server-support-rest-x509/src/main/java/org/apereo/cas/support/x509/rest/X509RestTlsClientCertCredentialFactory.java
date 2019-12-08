package org.apereo.cas.support.x509.rest;

import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link X509RestTlsClientCertCredentialFactory} that attempts to
 * fetch the TLS client certificate from the servlet container, to construct
 * X509 credentials.
 *
 * @author Stéphane Adenot
 * @since 6.0.0
 */
@Slf4j
public class X509RestTlsClientCertCredentialFactory implements RestHttpRequestCredentialFactory {

    private static final String REQUEST_ATTRIBUTE_X509_CERTIFICATE = "javax.servlet.request.X509Certificate";

    @Override
    public List<Credential> fromRequest(final HttpServletRequest request, final MultiValueMap<String, String> requestBody) {

        val certificates = (X509Certificate[]) request.getAttribute(REQUEST_ATTRIBUTE_X509_CERTIFICATE);

        if (certificates != null && certificates.length > 0) {
            LOGGER.debug("Certificates found in request attribute: {}", REQUEST_ATTRIBUTE_X509_CERTIFICATE);
            val credentials = new ArrayList<Credential>(1);
            credentials.add(new X509CertificateCredential(certificates));
            return credentials;
        }
        return new ArrayList<>(0);
    }
}
