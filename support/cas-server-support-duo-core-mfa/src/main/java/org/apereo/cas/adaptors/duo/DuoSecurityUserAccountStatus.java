package org.apereo.cas.adaptors.duo;

import java.util.Locale;

/**
 * This is {@link DuoSecurityUserAccountStatus}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public enum DuoSecurityUserAccountStatus {
    /**
     * The user is known and permitted to authenticate.
     */
    AUTH,
    /**
     * The user is configured to bypass secondary authentication.
     */
    ALLOW,
    /**
     * The user is not permitted to authenticate at this time.
     */
    DENY,
    /**
     * The user is not known to Duo and needs to enroll.
     */
    ENROLL,
    /**
     * Duo service was unavailable.
     */
    UNAVAILABLE;

    /**
     * Translate status to a duo account status type.
     *
     * @param status the status
     * @return the duo security user account status
     */
    public static DuoSecurityUserAccountStatus from(final String status) {
        return switch (status.toLowerCase(Locale.ENGLISH)) {
            case "bypass" -> ALLOW;
            case "disabled", "locked", "pending_deletion" -> DENY;
            default -> AUTH;
        };
    }

    /**
     * Convert status to string value understood by Duo Security.
     *
     * @return the string
     */
    public String toValue() {
        return switch (this) {
            case ALLOW -> "bypass";
            case DENY -> "disabled";
            case UNAVAILABLE -> "locked out";
            default -> "active";
        };
    }
}
