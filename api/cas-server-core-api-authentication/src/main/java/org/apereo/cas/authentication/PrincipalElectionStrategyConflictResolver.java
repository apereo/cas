package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;

import java.util.List;
import java.util.Map;

/**
 * This is {@link PrincipalElectionStrategyConflictResolver}
 * that determines the final principal identifier
 * as part of the principal election strategy.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@FunctionalInterface
public interface PrincipalElectionStrategyConflictResolver {

    /**
     * Pick the last principal in the chain of principals resolved.
     *
     * @return the principal election strategy conflict resolver
     */
    static PrincipalElectionStrategyConflictResolver last() {
        return (principals, attributes) -> principals.get(principals.size() - 1).getId();
    }

    /**
     * Pick the first principal in the chain of principals resolved.
     *
     * @return the principal election strategy conflict resolver
     */
    static PrincipalElectionStrategyConflictResolver first() {
        return (principals, attributes) -> principals.get(0).getId();
    }

    /**
     * Resolve the principal id from the chain.
     *
     * @param principals the principals chain
     * @param attributes the attributes
     * @return the final principal id
     */
    String resolve(List<Principal> principals, Map<String, List<Object>> attributes);
}
