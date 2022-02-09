package org.apereo.cas.authentication.support.password;

import org.apereo.cas.DefaultMessageDescriptor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Message conveying account password expiration warning details.
 *
 * @author Misagh Moayyed
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class PasswordExpiringWarningMessageDescriptor extends DefaultMessageDescriptor {
    private static final long serialVersionUID = -5892600936676838470L;

    /**
     * Message bundle code.
     */
    private static final String CODE = "password.expiration.warning";

    @JsonCreator
    public PasswordExpiringWarningMessageDescriptor(
        @JsonProperty("message")
        final String message,
        @JsonProperty("days")
        final long days) {
        super(CODE, message, new Serializable[]{days});
    }

    public long getDaysToExpiration() {
        return (Long) getParams()[0];
    }
}
