package org.apereo.cas.adaptors.yubikey;

/**
 * This is {@link DenyAllYubiKeyAccountValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class DenyAllYubiKeyAccountValidator implements YubiKeyAccountValidator {
    @Override
    public boolean isValid(final String uid, final String token) {
        return false;
    }
}
