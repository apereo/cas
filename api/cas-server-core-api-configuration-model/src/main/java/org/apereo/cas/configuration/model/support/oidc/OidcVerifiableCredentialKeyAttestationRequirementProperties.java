package org.apereo.cas.configuration.model.support.oidc;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Key attestation requirements of a credential configuration, advertised as {@code key_attestations_required}
 * in its {@code proof_types_supported} (OpenID4VCI 1.0 Appendix D).
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiresModule(name = "cas-server-support-oidc-vc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcVerifiableCredentialKeyAttestationRequirementProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 4236647635085072105L;

    /**
     * Whether every proof for this credential configuration must come with a key attestation, either in the
     * {@code key_attestation} header of a {@code jwt} proof or as an {@code attestation} proof.
     */
    private boolean required;

    /**
     * Attack potential resistance levels of the key storage this credential configuration accepts, such as
     * {@code iso_18045_high} or {@code iso_18045_moderate}. When set, a key attestation must name at least one of them
     * in its {@code key_storage} claim.
     */
    private List<String> keyStorage = new ArrayList<>();

    /**
     * Attack potential resistance levels of user authentication this credential configuration accepts, such as
     * {@code iso_18045_high}. When set, a key attestation must name at least one of them in its
     * {@code user_authentication} claim.
     */
    private List<String> userAuthentication = new ArrayList<>();
}
