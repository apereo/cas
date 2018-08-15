package org.apereo.cas.support.x509.rest;

import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.web.extractcert.X509CertificateExtractor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link X509RestHttpRequestHeaderCredentialFactory} that attempts to
 * extract the certificate from the request, using {@link X509CertificateExtractor}
 * to locate and construct X509 credentials. If the request does not contain a
 * certificate, it will then fallback onto the default behavior of capturing
 * credentials.
 *
 * @author Curtis Ruck
 * @since 6.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class X509RestHttpRequestHeaderCredentialFactory implements RestHttpRequestCredentialFactory {

    private static final String CERTIFICATE = "cert";

    private final X509CertificateExtractor certificateExtractor;

    @Override
    public List<Credential> fromRequest(final HttpServletRequest request, final MultiValueMap<String, String> requestBody) {
        final List<Credential> credentials = new ArrayList<Credential>();
        final X509Certificate[] certFromHeader = certificateExtractor.extract(request);
        if (certFromHeader != null) {
            LOGGER.debug("Certificate found in HTTP request via {}", certificateExtractor.getClass().getName());
            credentials.add(new X509CertificateCredential(certFromHeader));
        }
        return credentials;
    }
}
