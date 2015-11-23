package org.jasig.cas.authentication.support;

import org.jasig.cas.DefaultMessageDescriptor;

/**
 * Message conveying account password expiration warning details.
 *
 * @author Misagh Moayyed
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class PasswordExpiringWarningMessageDescriptor extends DefaultMessageDescriptor {
    /** Serialization version marker. */
    private static final long serialVersionUID = -5892600936676838470L;

    /** Message bundle code. */
    private static final String CODE = "password.expiration.warning";

    /**
     * Creates a new instance.
     *
     * @param defaultMsg  Default warning message.
     * @param days Days to password expiration.
     * @param passwordChangeUrl Password change URL.
     */
    public PasswordExpiringWarningMessageDescriptor(final String defaultMsg, final long days, final String passwordChangeUrl) {
        super(CODE, defaultMsg, days, passwordChangeUrl);
    }

    public long getDaysToExpiration() {
        return (Long) getParams()[0];
    }

    public String getPasswordChangeUrl() {
        return (String) getParams()[1];
    }
}
