package org.apereo.cas.configuration.model.support.oidc.federation;

import module java.base;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link OidcFederationTrustMarkOwnerProperties}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcFederationTrustMarkOwnerProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -7735226017372516348L;

    /**
     * The trust mark type identifier.
     */
    @RequiredProperty
    private String trustMarkType;

    /**
     * Entity identifier of the owner of the trust mark type, who delegates its issuance to trust mark issuers.
     */
    @RequiredProperty
    private String owner;

    /**
     * The keys of the owner, with which delegations are verified, as a JSON web key set or a resource such as
     * a file or a URL that holds one.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String jwks;
}
