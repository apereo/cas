package org.apereo.cas.adaptors.u2f;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link U2FRegistration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public record U2FRegistration(String challenge, String appId, String requestId, String principalId, String jsonData) implements Serializable {
    @Serial
    private static final long serialVersionUID = 8478965906212939618L;

    public String getVersion() {
        return "U2F_V2";
    }
}
