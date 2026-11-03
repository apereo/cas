package org.apereo.cas.vc.presentation;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialsPresentationProperties.ClientIdentifierPrefixes;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreUtils;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.vc.presentation.OidcVerifiableCredentialPresentationRequestEndpointController.OidcVerifiableCredentialPresentationClientMetadata;
import org.apereo.cas.vc.presentation.OidcVerifiableCredentialPresentationRequestEndpointController.OidcVerifiableCredentialPresentationClientMetadata.VpFormat;
import com.authlete.cose.constants.COSEAlgorithms;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.Use;
import org.jspecify.annotations.Nullable;

/**
 * Describes CAS as an OpenID4VP verifier: the formats it accepts, where wallets answer, and the keys it signs request
 * objects with and receives encrypted responses on. The same description is sent as {@code client_metadata} in
 * requests and, under the {@code openid_federation} client identifier prefix, published as
 * {@code openid_credential_verifier} metadata in the OpenID Federation entity configuration of CAS (OpenID Federation
 * for Wallet Architectures, section 6.3), where wallets must take it from instead.
 * <p>
 * Under that prefix, responses are encrypted to the encryption keys of the OpenID Connect keystore, published with
 * the JWE algorithm each is used with: {@code ECDH-ES} for an elliptic curve key, as HAIP requires, and
 * {@code RSA-OAEP-256} for an RSA key. Keys without a key id are never published, since wallets name the key they
 * encrypt to by its {@code kid}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class OidcVerifiableCredentialVerifierMetadataService {
    /**
     * Default bean name.
     */
    public static final String BEAN_NAME = "oidcVerifiableCredentialVerifierMetadataService";

    /**
     * OpenID Federation entity type of a credential verifier.
     */
    public static final String ENTITY_TYPE_CREDENTIAL_VERIFIER = "openid_credential_verifier";

    private static final String VERIFIER_NAME = "Apereo CAS";

    protected final OidcConfigurationContext configurationContext;

    protected final LoadingCache<OidcJsonWebKeyCacheKey, JsonWebKeySet> keystoreCache;

    /**
     * Whether wallets authenticate CAS through OpenID Federation, with the {@code openid_federation} client identifier
     * prefix.
     *
     * @return true/false
     */
    public boolean isFederationVerifier() {
        return configurationContext.getCasProperties().getAuthn().getOidc().getVc().getPresentation()
            .getClientIdentifierPrefix() == ClientIdentifierPrefixes.OPENID_FEDERATION;
    }

    /**
     * Presentation formats the verifier accepts: {@code dc+sd-jwt} with the signing algorithms of the {@code dc+sd-jwt}
     * credential configurations, and {@code mso_mdoc} when a credential configuration issues mdocs.
     *
     * @return the formats, keyed by format identifier
     */
    public Map<String, VpFormat> buildVpFormatsSupported() {
        val vpFormats = new LinkedHashMap<String, VpFormat>();
        vpFormats.put(OidcVerifiableCredentialConfigurationProperties.CredentialConfigurationFormats.DC_SD_JWT.getValue(),
            VpFormat.builder()
                .sdJwtAlgValues(resolveCredentialSigningAlgorithms())
                .kbJwtAlgValues(OidcVerifiableCredentialPresentationResponseEndpointController.KEY_BINDING_ALGORITHMS_SUPPORTED)
                .build());
        val issuerAuthAlgorithms = resolveMdocSigningAlgorithms();
        if (!issuerAuthAlgorithms.isEmpty()) {
            vpFormats.put(OidcVerifiableCredentialConfigurationProperties.CredentialConfigurationFormats.MSO_MDOC.getValue(),
                VpFormat.builder()
                    .issuerAuthAlgValues(issuerAuthAlgorithms)
                    .deviceAuthAlgValues(OidcVerifiableCredentialMdocPresentationVerifier.DEVICE_SIGNING_ALGORITHMS)
                    .build());
        }
        return vpFormats;
    }

    /**
     * Credential signing algorithms the verifier accepts, advertised as {@code sd-jwt_alg_values}: those of the
     * {@code dc+sd-jwt} credential configurations, since a presented credential is checked against the
     * configuration its {@code vct} names.
     *
     * @return the credential signing algorithms
     */
    public List<String> resolveCredentialSigningAlgorithms() {
        return configurationContext.getCasProperties().getAuthn().getOidc().getVc().getIssuer().getCredentialConfigurations()
            .values()
            .stream()
            .filter(configuration -> configuration.getFormat()
                == OidcVerifiableCredentialConfigurationProperties.CredentialConfigurationFormats.DC_SD_JWT)
            .flatMap(configuration -> configuration.getCredentialSigningAlgValuesSupported().stream())
            .distinct()
            .toList();
    }

    /**
     * Issuer signature algorithms the verifier accepts for an mdoc, advertised as {@code issuerauth_alg_values}: the COSE
     * identifiers of those the {@code mso_mdoc} credential configurations advertise, since a presented mdoc is checked
     * against the configuration its document type names. Empty when no configuration issues an mdoc.
     *
     * @return the COSE algorithm identifiers
     */
    public List<Integer> resolveMdocSigningAlgorithms() {
        return configurationContext.getCasProperties().getAuthn().getOidc().getVc().getIssuer().getCredentialConfigurations()
            .values()
            .stream()
            .filter(configuration -> configuration.getFormat()
                == OidcVerifiableCredentialConfigurationProperties.CredentialConfigurationFormats.MSO_MDOC)
            .flatMap(configuration -> configuration.getCredentialSigningAlgValuesSupported().stream())
            .filter(OidcVerifiableCredentialMdocPresentationVerifier.ISSUER_SIGNING_ALGORITHMS::contains)
            .map(COSEAlgorithms::getValueByName)
            .distinct()
            .toList();
    }

    /**
     * The {@code openid_credential_verifier} metadata CAS publishes in its entity configuration under the
     * {@code openid_federation} client identifier prefix: the verifier description of {@code client_metadata}, the
     * response URI as the only trusted {@code response_uris}, and in {@code jwks} the keys request objects are signed
     * with and the keys responses are encrypted to. No {@code request_uris} are listed: CAS serves each request object
     * at a URI of its own, and wallets refuse any request URI the list does not contain.
     *
     * @return the metadata
     */
    public OidcVerifiableCredentialPresentationClientMetadata buildFederationMetadata() {
        val keys = new ArrayList<Map<String, Object>>();
        getRequestObjectSigningKeys().forEach(key -> keys.add(toPublishedKey(key, Use.SIGNATURE, OidcJsonWebKeyStoreUtils.getSigningAlgorithm(key))));
        getResponseEncryptionKeys().forEach(key -> keys.add(toPublishedKey(key, Use.ENCRYPTION, resolveKeyManagementAlgorithm(key))));
        return OidcVerifiableCredentialPresentationClientMetadata.builder()
            .clientName(VERIFIER_NAME)
            .vpFormatsSupported(buildVpFormatsSupported())
            .responseUris(List.of(OidcVerifiableCredentialPresentationRequestEndpointController.resolveResponseUri(
                configurationContext.getCasProperties())))
            .jwks(Map.of(JsonWebKeySet.JWK_SET_MEMBER_NAME, keys))
            .encryptedResponseEncValuesSupported(
                getResponseEncryptionKeys().isEmpty()
                    ? null
                    : OidcVerifiableCredentialPresentationResponseEndpointController.RESPONSE_ENCRYPTION_ALGORITHMS_SUPPORTED)
            .build();
    }

    /**
     * Encryption keys of the OpenID Connect keystore that a wallet can encrypt a response to: keys with a key id, a
     * private key and a type a JWE algorithm is known for.
     *
     * @return the keys, with their private keys
     */
    public List<PublicJsonWebKey> getResponseEncryptionKeys() {
        return getKeystoreKeys(OidcJsonWebKeyUsage.ENCRYPTION)
            .filter(key -> key.getPrivateKey() != null && resolveKeyManagementAlgorithm(key) != null)
            .toList();
    }

    /**
     * The encryption key of the OpenID Connect keystore that a response names with its {@code kid}.
     *
     * @param keyId the key id
     * @return the key, if it is one responses can be encrypted to
     */
    public Optional<PublicJsonWebKey> findResponseEncryptionKey(final @Nullable String keyId) {
        return StringUtils.isBlank(keyId)
            ? Optional.empty()
            : getResponseEncryptionKeys().stream().filter(key -> keyId.equals(key.getKeyId())).findFirst();
    }

    /**
     * Signing keys of the OpenID Connect keystore, whose public keys verify the request objects CAS signs.
     *
     * @return the keys
     */
    protected List<PublicJsonWebKey> getRequestObjectSigningKeys() {
        return getKeystoreKeys(OidcJsonWebKeyUsage.SIGNING).toList();
    }

    private Stream<PublicJsonWebKey> getKeystoreKeys(final OidcJsonWebKeyUsage usage) {
        val issuer = configurationContext.getIssuerService().determineIssuer(Optional.empty());
        val keystore = keystoreCache.get(new OidcJsonWebKeyCacheKey(issuer, usage));
        if (keystore == null) {
            return Stream.empty();
        }
        return keystore.getJsonWebKeys()
            .stream()
            .filter(PublicJsonWebKey.class::isInstance)
            .map(PublicJsonWebKey.class::cast)
            .filter(key -> StringUtils.isNotBlank(key.getKeyId()));
    }

    /**
     * JWE algorithm a wallet encrypts a response with, given the encryption key.
     *
     * @param key the encryption key
     * @return the JWE algorithm, or null for a key type responses cannot be encrypted to
     */
    public static @Nullable String resolveKeyManagementAlgorithm(final JsonWebKey key) {
        return switch (key) {
            case RsaJsonWebKey _ -> KeyManagementAlgorithmIdentifiers.RSA_OAEP_256;
            case EllipticCurveJsonWebKey _ -> KeyManagementAlgorithmIdentifiers.ECDH_ES;
            default -> null;
        };
    }

    private static Map<String, Object> toPublishedKey(final PublicJsonWebKey key, final String use, final @Nullable String algorithm) {
        val parameters = new LinkedHashMap<>(key.toParams(JsonWebKey.OutputControlLevel.PUBLIC_ONLY));
        parameters.put(JsonWebKey.USE_PARAMETER, use);
        parameters.put(JsonWebKey.ALGORITHM_PARAMETER, algorithm);
        return parameters;
    }
}
