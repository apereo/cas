package org.apereo.cas.oidc.discovery;

import module java.base;

/**
 * Contributes the metadata of entity types that CAS plays besides {@code openid_provider} to its OpenID Federation
 * entity configuration, such as {@code openid_credential_issuer} when CAS issues verifiable credentials. Modules
 * declare implementations as beans, and the federation module, when present, publishes what they contribute under
 * the entity configuration's {@code metadata}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@FunctionalInterface
public interface OidcFederationEntityMetadataContributor {

    /**
     * Metadata to publish, keyed by entity type identifier. Each value is the metadata of that entity type as a JSON
     * object. The entity types {@code openid_provider} and {@code federation_entity} are built by the federation
     * module and cannot be contributed.
     *
     * @param entityId the entity identifier of this deployment
     * @return the metadata by entity type, or an empty map when there is nothing to publish
     */
    Map<String, Map<String, Object>> contribute(String entityId);
}
