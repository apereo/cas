package org.apereo.cas.adaptors.duo.authn;

import module java.base;
import org.apereo.cas.adaptors.duo.DuoSecurityBypassCode;
import org.apereo.cas.adaptors.duo.DuoSecurityUserAccount;

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
    default Optional<DuoSecurityUserAccount> getDuoSecurityUserAccount(final String username) throws Exception {
        return getDuoSecurityUserAccount(username, true);
    }

    /**
     * Gets duo security user account.
     *
     * @param username         the username
     * @param fetchBypassCodes the fetch bypass codes
     * @return the duo security user account
     * @throws Exception the exception
     */
    Optional<DuoSecurityUserAccount> getDuoSecurityUserAccount(String username, boolean fetchBypassCodes) throws Exception;

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

    /**
     * Modify duo security user account.
     *
     * @param newAccount       the new account
     * @return the optional
     * @throws Exception the exception
     */
    Optional<DuoSecurityUserAccount> modifyDuoSecurityUserAccount(DuoSecurityUserAccount newAccount) throws Exception;

    /**
     * Delete duo security user account.
     *
     * @param userIdentifier the user identifier
     * @param deviceId       the device id
     * @throws Exception the exception
     */
    void deleteDuoSecurityUserDevice(String userIdentifier, String deviceId) throws Exception;
}
