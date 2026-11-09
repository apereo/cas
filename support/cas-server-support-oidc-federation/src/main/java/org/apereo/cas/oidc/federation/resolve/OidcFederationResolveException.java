package org.apereo.cas.oidc.federation.resolve;

import module java.base;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * An entity that cannot be resolved, with the error the resolve endpoint answers (OpenID Federation 1.0, section 8.9).
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Getter
public class OidcFederationResolveException extends Exception {
    @Serial
    private static final long serialVersionUID = -2860125784013517341L;

    private final HttpStatus status;

    private final String error;

    public OidcFederationResolveException(final HttpStatus status, final String error, final String description) {
        super(description);
        this.status = status;
        this.error = error;
    }
}
