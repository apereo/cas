package org.apereo.cas.configuration.model.support.oidc.federation;

import module java.base;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link OidcFederationTrustMarkIssuerProperties}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcFederationTrustMarkIssuerProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -2468871226537412361L;

    /**
     * The trust mark type identifier.
     */
    @RequiredProperty
    private String trustMarkType;

    /**
     * Entity identifiers of the entities accepted as issuers of trust marks of this type.
     * When empty, anyone may issue trust marks of this type.
     */
    private List<String> issuers = new ArrayList<>();
}
