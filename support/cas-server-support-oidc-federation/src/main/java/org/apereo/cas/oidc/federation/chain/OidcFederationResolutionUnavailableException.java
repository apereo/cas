package org.apereo.cas.oidc.federation.chain;

import module java.base;
import lombok.experimental.StandardException;

/**
 * A resolution that could not be completed for reasons that say nothing about the entity, such as too many resolutions
 * running at once or a trust anchor that cannot be reached for the moment. The entity must not be remembered as
 * unresolved on that account.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@StandardException
public class OidcFederationResolutionUnavailableException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -4416360183870349126L;
}
