package org.apereo.cas.configuration.model.support.oidc;

import module java.base;
import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link OidcVerifiableCredentialsOfferProperties}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiresModule(name = "cas-server-support-oidc-vc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcVerifiableCredentialsOfferProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -2220371070424785548L;

    /**
     * The principal attribute that must be present in the authenticated principal for the offer to be valid.
     * If not set, the offer is valid for all authenticated principals.
     */
    private String requiredPrincipalAttribute;
    /**
     * The value of the principal attribute that must be present in the authenticated principal for the offer to be valid.
     * If not set, the offer is valid for all authenticated principals.
     */
    @RegularExpressionCapable
    private String requiredPrincipalAttributeValue;

    /**
     * Whether a transaction code should be attached to credential offers that use
     * the pre-authorized code flow. The code is a secret handed to the issuing party
     * out of band and must be presented back at the token endpoint. Disable this only
     * for wallets that cannot collect the code from the end user, keeping in mind that
     * the pre-authorized code then becomes the only secret protecting the offer.
     */
    private boolean transactionCodeEnabled = true;
}
