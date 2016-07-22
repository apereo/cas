package org.apereo.cas.adaptors.authy;

import org.apereo.cas.authentication.principal.Principal;

/**
 * General interface that defines how authy ids
 * as part of the registration process are managed
 * and maintained.
 * @author Misagh Moayyed
 * @since 4.2
 */
public interface AuthyAccountRegistry {

    /**
     * Add a user to authy account store.
     *
     * @param authyId the authy id
     * @param principal the principal
     */
    void add(Integer authyId, Principal principal);

    /**
     * Gets authy id by principal.
     *
     * @param principal the principal
     * @return the authy id by principal
     */
    Integer get(Principal principal);

    /**
     * Contains the authy id for this principal?
     *
     * @param principal the principal
     * @return true/false
     */
    boolean contains(Principal principal);
}
