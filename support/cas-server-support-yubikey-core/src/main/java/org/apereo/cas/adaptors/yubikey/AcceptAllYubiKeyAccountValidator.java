package org.apereo.cas.adaptors.yubikey;

/**
 * This is {@link AcceptAllYubiKeyAccountValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class AcceptAllYubiKeyAccountValidator implements YubiKeyAccountValidator {
    @Override
    public boolean isValid(final String uid, final String token) {
        return true;
    }
}
