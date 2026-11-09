package org.apereo.cas.configuration.model.support.oidc.federation;

import module java.base;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link OidcFederationTrustAnchorProperties}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcFederationTrustAnchorProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 6326212841963217581L;

    /**
     * The entity identifier of the trust anchor.
     */
    @RequiredProperty
    private String entityId;

    /**
     * The public keys of the trust anchor, obtained out of band, as a JSON web key set:
     * a resource such as a file or a URL, or the JSON document itself. Private key members are ignored.
     * When undefined, the keys are taken from the trust anchor's own entity configuration,
     * which trusts whoever answers at its entity identifier.
     */
    @ExpressionLanguageCapable
    private String jwks;
}
