package org.apereo.cas.adaptors.u2f;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * This is {@link U2FRegistration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Getter
@RequiredArgsConstructor
public class U2FRegistration implements Serializable {
    private static final long serialVersionUID = 8478965906212939618L;

    private final String challenge;

    private final String appId;

    private final String requestId;

    private final String principalId;

    private final String jsonData;

    public String getVersion() {
        return "U2F_V2";
    }
}
