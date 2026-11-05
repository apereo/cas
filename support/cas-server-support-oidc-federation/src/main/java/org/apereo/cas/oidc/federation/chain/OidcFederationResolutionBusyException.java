package org.apereo.cas.oidc.federation.chain;

import module java.base;
import lombok.experimental.StandardException;

/**
 * A resolution that could not start because the configured number of resolutions is already running. It says nothing
 * about the entity, which must not be remembered as unresolved on that account.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@StandardException
public class OidcFederationResolutionBusyException extends OidcFederationResolutionUnavailableException {
    @Serial
    private static final long serialVersionUID = 4263904761735829043L;

}
