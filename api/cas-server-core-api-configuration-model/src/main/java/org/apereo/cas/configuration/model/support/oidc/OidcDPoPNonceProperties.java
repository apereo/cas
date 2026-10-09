package org.apereo.cas.configuration.model.support.oidc;

import module java.base;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Server-provided nonces for DPoP proofs (RFC 9449, sections 8 and 9). Once turned on, every DPoP proof must carry, in its
 * {@code nonce} claim, a nonce that CAS handed out and that has not expired.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcDPoPNonceProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -6620148315920372781L;

    /**
     * Whether DPoP proofs must carry a nonce that CAS handed out. A proof without one, or with an unknown or expired one, is
     * refused with {@code use_dpop_nonce} and a fresh nonce in the {@code DPoP-Nonce} header: with {@code 400} at the token
     * endpoint and in the DPoP combined mode of attestation-based client authentication, and with {@code 401} and a
     * {@code WWW-Authenticate: DPoP} challenge at protected resources. Nonces are also handed out by the OpenID4VCI nonce
     * endpoint and the client attestation challenge endpoint.
     */
    private boolean enabled;

    /**
     * How long a nonce may be used. A nonce may be used more than once while it is valid; the {@code jti} of each proof still
     * prevents a proof from being replayed.
     */
    @DurationCapable
    private String timeToLive = "PT5M";
}
