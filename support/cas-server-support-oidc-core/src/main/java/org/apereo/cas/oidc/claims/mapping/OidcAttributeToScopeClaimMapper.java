package org.apereo.cas.oidc.claims.mapping;

/**
 * This is {@link OidcAttributeToScopeClaimMapper}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface OidcAttributeToScopeClaimMapper {
    /**
     * The bean name of the default implementation.
     */
    String DEFAULT_BEAN_NAME = "oidcAttributeToScopeClaimMapper";

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
     * @return true/false
     */
    boolean containsMappedAttribute(String claim);
}
