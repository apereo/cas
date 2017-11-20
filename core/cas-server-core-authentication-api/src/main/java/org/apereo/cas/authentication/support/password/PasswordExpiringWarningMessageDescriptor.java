package org.apereo.cas.authentication.support.password;

import org.apereo.cas.DefaultMessageDescriptor;

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
     */
    public PasswordExpiringWarningMessageDescriptor(final String defaultMsg, final long days) {
        super(CODE, defaultMsg, days);
    }

    public long getDaysToExpiration() {
        return (Long) getParams()[0];
    }
    
}
