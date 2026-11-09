package org.apereo.cas.oidc.federation.trustmark;

import module java.base;
import com.nimbusds.jwt.SignedJWT;
import org.jspecify.annotations.Nullable;

/**
 * Issues the trust marks of this entity as a trust mark issuer (OpenID Federation 1.0, section 7), and answers the
 * trust mark (section 8.6), trust mark status (section 8.4) and trust marked entities listing (section 8.5) requests.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public interface OidcFederationTrustMarkIssuer {
    /**
     * Bean name.
     */
    String BEAN_NAME = "oidcFederationTrustMarkIssuer";

    /**
     * Whether this entity issues trust marks.
     *
     * @return true or false
     */
    boolean isTrustMarkIssuer();

    /**
     * Issue a trust mark of the given type to the given entity.
     *
     * @param trustMarkType the trust mark type
     * @param subject       the entity identifier of the subject
     * @return the signed trust mark, or empty when the entity does not hold a trust mark of this type
     * @throws Exception the exception
     */
    Optional<SignedJWT> issue(String trustMarkType, String subject) throws Exception;

    /**
     * The entities that hold a trust mark of the given type.
     *
     * @param trustMarkType the trust mark type
     * @param subject       the entity identifier to filter the listing on, if any
     * @return the entity identifiers, or empty when this entity issues no trust marks of this type
     */
    Optional<List<String>> list(String trustMarkType, @Nullable String subject);

    /**
     * The status of a trust mark this entity issued, as a signed trust mark status response.
     *
     * @param trustMark the serialized trust mark
     * @return the signed status response, or empty when the trust mark was not issued by this entity
     * @throws Exception when the trust mark cannot be parsed
     */
    Optional<SignedJWT> status(String trustMark) throws Exception;
}
