package org.apereo.cas.configuration.model.support.oidc;

import module java.base;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Server-provided challenges for attestation-based client authentication. Once challenges are offered, the proof of
 * possession of every client attestation must carry one that CAS handed out and that has not expired.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcClientAttestationChallengeProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 1938475620194837162L;

    /**
     * Whether CAS hands out challenges at its challenge endpoint, advertised as {@code challenge_endpoint}, and requires
     * the {@code challenge} claim of every client attestation proof of possession to carry one. A missing, unknown or
     * expired challenge is answered with {@code use_attestation_challenge} and a fresh challenge in the
     * {@code OAuth-Client-Attestation-Challenge} header.
     */
    private boolean enabled;

    /**
     * How long a challenge may be used. A challenge may be used more than once while it is valid; the {@code jti} of each
     * proof of possession still prevents a proof from being replayed.
     */
    @DurationCapable
    private String timeToLive = "PT5M";
}
