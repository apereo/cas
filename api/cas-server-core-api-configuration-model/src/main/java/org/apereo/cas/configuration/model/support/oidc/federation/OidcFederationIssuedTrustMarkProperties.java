package org.apereo.cas.configuration.model.support.oidc.federation;

import module java.base;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link OidcFederationIssuedTrustMarkProperties}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcFederationIssuedTrustMarkProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 4180338519436617284L;

    /**
     * The trust mark type identifier of the trust marks issued.
     */
    @RequiredProperty
    private String trustMarkType;

    /**
     * Entity identifiers of the entities this trust mark type is issued to. Removing an entity revokes the
     * trust marks of this type issued to it.
     */
    @RequiredProperty
    private List<String> subjects = new ArrayList<>();

    /**
     * How long an issued trust mark is valid. A zero or negative duration issues trust marks that do not expire.
     */
    @DurationCapable
    private String expiration = "P30D";

    /**
     * URL of a logo for the trust mark, published as {@code logo_uri}.
     */
    private String logoUri;

    /**
     * URL of human-readable information about the issuance of the trust mark, published as {@code ref}.
     */
    private String ref;

    /**
     * The delegation that allows this entity to issue trust marks of a type it does not own, a signed JWT
     * obtained from the owner of the type, given as a resource such as a file or a URL, or the JWT itself.
     */
    @ExpressionLanguageCapable
    private String delegation;
}
