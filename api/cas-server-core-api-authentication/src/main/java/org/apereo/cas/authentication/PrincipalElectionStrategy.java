package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;

import org.springframework.core.Ordered;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This is {@link PrincipalElectionStrategy} that attempts to nominate a given principal
 * as the primary principal object amongst many authentication events.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public interface PrincipalElectionStrategy extends Serializable, Ordered {

    /**
     * Elect the principal.
     *
     * @param authentications     the authentications
     * @param principalAttributes the principal attributes
     * @return the principal
     */
    Principal nominate(Collection<Authentication> authentications, Map<String, List<Object>> principalAttributes);

    /**
     * Nominate principal.
     *
     * @param principals the principals
     * @param attributes the attributes
     * @return the principal
     */
    Principal nominate(List<Principal> principals, Map<String, List<Object>> attributes);

    @Override
    default int getOrder() {
        return Integer.MAX_VALUE;
    }
}
