package org.apereo.cas.support.oauth.validator;

import module java.base;

/**
 * This is {@link CertificateBoundAccessTokenRequiredException}, thrown when a client registered with
 * {@code tls_client_certificate_bound_access_tokens} makes a token request that is not authenticated with mutual TLS,
 * so that no certificate-bound access token could be issued to it (RFC 8705, section 3.4).
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class CertificateBoundAccessTokenRequiredException extends Exception {
    @Serial
    private static final long serialVersionUID = -5164209573120936615L;

    public CertificateBoundAccessTokenRequiredException(final String message) {
        super(message);
    }
}
