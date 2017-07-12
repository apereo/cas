package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * This is {@link PrincipalElectionStrategy} that attempts to nominate a given principal
 * as the primary principal object amongst many authentication events.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@FunctionalInterface
public interface PrincipalElectionStrategy extends Serializable {

    /**
     * Elect the principal.
     *
     * @param authentications     the authentications
     * @param principalAttributes the principal attributes
     * @return the principal
     */
    Principal nominate(Collection<Authentication> authentications, Map<String, Object> principalAttributes);
}
