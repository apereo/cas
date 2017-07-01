package org.apereo.cas.adaptors.yubikey;

/**
 * This is {@link YubiKeyAccountValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SuppressWarnings("ALL")
public interface YubiKeyAccountValidator {

    /**
     * Is account/device valid ?.
     *
     * @param uid   the uid
     * @param token the token
     * @return the boolean
     */
    boolean isValid(String uid, String token);
}
