package org.apereo.cas.configuration.model.support.oidc;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Key attestation settings of the verifiable credential issuer, per OpenID4VCI 1.0 Appendix D.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiresModule(name = "cas-server-support-oidc-vc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcVerifiableCredentialKeyAttestationProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 4120371070424785548L;

    /**
     * Locations of PEM-encoded certificates of the trust anchors that key attestations must chain to, such as those
     * of wallet providers. A key attestation carries its signing certificate, and any intermediate certificates, in
     * its {@code x5c} header, which must lead to one of these anchors. Key attestations are not accepted, and the
     * {@code attestation} proof type is not offered, until at least one trust anchor is configured.
     */
    private List<String> trustAnchors = new ArrayList<>();
}
