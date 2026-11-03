package org.apereo.cas.oidc.vc.federation;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.discovery.OidcFederationEntityMetadataContributor;
import org.apereo.cas.oidc.vc.issuer.metadata.OidcCredentialIssuerMetadataService;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.vc.presentation.OidcVerifiableCredentialVerifierMetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Publishes the verifiable credential roles of CAS in its OpenID Federation entity configuration (OpenID Federation
 * for Wallet Architectures, section 6): the credential issuer metadata under {@code openid_credential_issuer}, whose
 * {@code credential_issuer} must be the entity identifier, and the authorization server metadata under
 * {@code oauth_authorization_server}, since {@code authorization_servers} names CAS itself and each authorization
 * server it names must be a federation entity of that type. When wallets authenticate CAS as a verifier with the
 * {@code openid_federation} client identifier prefix, the verifier metadata is published under
 * {@code openid_credential_verifier} as well. When the issuer of CAS is not the entity identifier, for example because
 * the issuer is resolved per tenant, nothing is published.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcVerifiableCredentialFederationMetadataContributor implements OidcFederationEntityMetadataContributor {
    /**
     * Entity type of a credential issuer.
     */
    public static final String ENTITY_TYPE_CREDENTIAL_ISSUER = "openid_credential_issuer";

    /**
     * Entity type of an OAuth authorization server.
     */
    public static final String ENTITY_TYPE_AUTHORIZATION_SERVER = "oauth_authorization_server";

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false)
        .minimal(true)
        .build()
        .toObjectMapper();

    private static final TypeReference<Map<String, Object>> JSON_OBJECT = new TypeReference<>() {
    };

    private final OidcConfigurationContext configurationContext;

    private final OidcCredentialIssuerMetadataService issuerMetadataService;

    private final OidcVerifiableCredentialVerifierMetadataService verifierMetadataService;

    @Override
    public Map<String, Map<String, Object>> contribute(final String entityId) {
        val metadata = issuerMetadataService.build();
        if (!entityId.equals(metadata.getCredentialIssuer())) {
            LOGGER.warn("Credential issuer [{}] is not the federation entity identifier [{}] and is not published in the entity configuration",
                metadata.getCredentialIssuer(), entityId);
            return Map.of();
        }
        val contributed = new LinkedHashMap<String, Map<String, Object>>();
        contributed.put(ENTITY_TYPE_CREDENTIAL_ISSUER, MAPPER.convertValue(metadata, JSON_OBJECT));
        val discovery = configurationContext.getDiscoverySettings();
        if (metadata.getAuthorizationServers().contains(discovery.getIssuer())) {
            contributed.put(ENTITY_TYPE_AUTHORIZATION_SERVER, MAPPER.readValue(discovery.toJson(), JSON_OBJECT));
        }
        if (verifierMetadataService.isFederationVerifier()) {
            contributed.put(OidcVerifiableCredentialVerifierMetadataService.ENTITY_TYPE_CREDENTIAL_VERIFIER,
                MAPPER.convertValue(verifierMetadataService.buildFederationMetadata(), JSON_OBJECT));
        }
        return contributed;
    }
}
