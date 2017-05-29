package org.apereo.cas.adaptors.yubikey;

/**
 * This is {@link YubiKeyAccountValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public interface YubiKeyAccountValidator {

    /**
     * Is account/device valid ?.
     *
     * @param uid      the uid
     * @param publicId the public id
     * @return the boolean
     */
    boolean isValid(String uid, String publicId);
}
