package org.apereo.cas.authentication.support.password;

import module java.base;
import org.apereo.cas.DefaultMessageDescriptor;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Message conveying account password expiration warning details.
 *
 * @author Misagh Moayyed
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class PasswordExpiringWarningMessageDescriptor extends DefaultMessageDescriptor {
    @Serial
    private static final long serialVersionUID = -5892600936676838470L;

    /**
     * Message bundle code.
     */
    private static final String CODE = "password.expiration.warning";

    @JsonCreator
    public PasswordExpiringWarningMessageDescriptor(
        @JsonProperty("defaultMessage")
        final String defaultMessage,
        @JsonProperty("daysToExpiration")
        final long days) {
        super(CODE, defaultMessage, new Serializable[]{days});
    }

    public long getDaysToExpiration() {
        return (Long) getParams()[0];
    }

}
