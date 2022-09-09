package org.apereo.cas.adaptors.u2f;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link U2FAuthentication}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public record U2FAuthentication(String challenge, String appId, String keyHandle) implements Serializable {

    @Serial
    private static final long serialVersionUID = 334984331545697641L;

    public String getVersion() {
        return "U2F_V2";
    }
}
