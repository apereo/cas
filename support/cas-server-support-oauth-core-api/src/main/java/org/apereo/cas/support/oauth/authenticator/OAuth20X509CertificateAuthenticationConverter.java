package org.apereo.cas.support.oauth.authenticator;

import module java.base;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import jakarta.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;

/**
 * Reads the first client certificate of a mutual TLS connection from the {@code jakarta.servlet.request.X509Certificate}
 * request attribute.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class OAuth20X509CertificateAuthenticationConverter implements AuthenticationConverter {
    private static final String CERTIFICATE_REQUEST_ATTRIBUTE = "jakarta.servlet.request.X509Certificate";

    @Override
    public @Nullable Authentication convert(final HttpServletRequest request) {
        return request.getAttribute(CERTIFICATE_REQUEST_ATTRIBUTE) instanceof final X509Certificate[] certificates && certificates.length > 0
            ? new OAuth20ClientAuthenticationToken(request, OAuth20ClientAuthenticationToken.CredentialSource.CERTIFICATE, null, certificates[0])
            : null;
    }
}
