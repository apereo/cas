package org.apereo.cas.adaptors.yubikey;

import com.yubico.client.v2.YubicoClient;

/**
 * This is {@link YubiKeyAccountValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface YubiKeyAccountValidator {

    /**
     * Is account/device valid ?.
     *
     * @param uid   the uid
     * @param token the token
     * @return true/false
     */
    boolean isValid(String uid, String token);

    /**
     * Gets token public id.
     *
     * @param token the token
     * @return the token public id
     */
    default String getTokenPublicId(final String token) {
        return YubicoClient.getPublicId(token);
    }
}
