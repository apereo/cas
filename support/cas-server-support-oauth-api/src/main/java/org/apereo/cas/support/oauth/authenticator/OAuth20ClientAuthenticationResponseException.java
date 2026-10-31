package org.apereo.cas.support.oauth.authenticator;

import module java.base;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

/**
 * Thrown by a client authentication method that answers the request itself instead of letting the next method try,
 * such as a client attestation that needs a fresh challenge or a DPoP proof that needs a server-provided nonce.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Getter
public class OAuth20ClientAuthenticationResponseException extends AuthenticationException {
    @Serial
    private static final long serialVersionUID = -2493380541146470357L;

    private final int status;

    private final Map<String, String> headers;

    private final String body;

    public OAuth20ClientAuthenticationResponseException(final int status, final Map<String, String> headers, final String body) {
        super(body);
        this.status = status;
        this.headers = new LinkedHashMap<>(headers);
        this.body = body;
    }
}
