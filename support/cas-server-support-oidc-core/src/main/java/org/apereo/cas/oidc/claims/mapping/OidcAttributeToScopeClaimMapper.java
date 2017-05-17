package org.apereo.cas.oidc.claims.mapping;

/**
 * This is {@link OidcAttributeToScopeClaimMapper}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface OidcAttributeToScopeClaimMapper {
    /**
     * Gets mapped attribute.
     *
     * @param claim the claim
     * @return the mapped attribute
     */
    String getMappedAttribute(String claim);

    /**
     * Contains mapped attribute boolean.
     *
     * @param claim the claim
     * @return the boolean
     */
    boolean containsMappedAttribute(String claim);
}
