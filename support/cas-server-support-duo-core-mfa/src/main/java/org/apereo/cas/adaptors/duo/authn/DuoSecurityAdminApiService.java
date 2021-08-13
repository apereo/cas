package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.adaptors.duo.DuoSecurityUserAccount;

import java.util.Optional;

/**
 * This is {@link DuoSecurityAdminApiService}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@FunctionalInterface
public interface DuoSecurityAdminApiService {
    /**
     * Gets user.
     *
     * @param username the username
     * @return the user
     */
    Optional<DuoSecurityUserAccount> getUser(String username) throws Exception;
}
