package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.adaptors.duo.DuoSecurityBypassCode;
import org.apereo.cas.adaptors.duo.DuoSecurityUserAccount;

import java.util.List;
import java.util.Optional;

/**
 * This is {@link DuoSecurityAdminApiService}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public interface DuoSecurityAdminApiService {
    /**
     * Gets user.
     *
     * @param username the username
     * @return the user
     * @throws Exception the exception
     */
    Optional<DuoSecurityUserAccount> getDuoSecurityUserAccount(String username) throws Exception;

    /**
     * Gets duo security bypass codes for user id.
     *
     * @param userIdentifier the user identifier
     * @return the duo security bypass codes
     * @throws Exception the exception
     */
    List<DuoSecurityBypassCode> getDuoSecurityBypassCodesFor(String userIdentifier) throws Exception;

    /**
     * Gets duo security bypass codes for user id.
     *
     * @param userIdentifier the user identifier
     * @return the duo security bypass codes
     * @throws Exception the exception
     */
    List<Long> createDuoSecurityBypassCodesFor(String userIdentifier) throws Exception;
}
