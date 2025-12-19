package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.authentication.principal.Principal;

/**
 * This is {@link PrincipalElectionStrategyConflictResolver}
 * that determines the final principal identifier
 * as part of the principal election strategy.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@FunctionalInterface
public interface PrincipalElectionStrategyConflictResolver extends Serializable {

    /**
     * Default bean name.
     */
    String BEAN_NAME = "defaultPrincipalElectionStrategyConflictResolver";

    /**
     * Pick the last principal in the chain of principals resolved.
     *
     * @return the principal election strategy conflict resolver
     */
    static PrincipalElectionStrategyConflictResolver last() {
        return List::getLast;
    }

    /**
     * Pick the first principal in the chain of principals resolved.
     *
     * @return the principal election strategy conflict resolver
     */
    static PrincipalElectionStrategyConflictResolver first() {
        return List::getFirst;
    }

    /**
     * Resolve the principal id from the chain.
     *
     * @param principals the principals chain
     * @return the final principal id
     */
    Principal resolve(List<Principal> principals);
}
