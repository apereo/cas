package org.apereo.cas.adaptors.duo;

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
        switch (status.toLowerCase()) {
            case "bypass":
                return ALLOW;
            case "disabled":
            case "locked":
            case "pending_deletion":
                return DENY;
            default:
                return AUTH;
        }
    }
}
