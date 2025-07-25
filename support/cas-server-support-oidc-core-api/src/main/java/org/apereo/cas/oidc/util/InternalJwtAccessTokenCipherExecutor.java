package org.apereo.cas.oidc.util;

import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.oidc.token.OidcRegisteredServiceJwtCipherExecutor;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.token.cipher.JwtTicketCipherExecutor;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import java.io.Serializable;
import java.security.Key;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link InternalJwtAccessTokenCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Setter
@Getter
@Slf4j
public class InternalJwtAccessTokenCipherExecutor extends JwtTicketCipherExecutor {
    private JsonWebKey signingWebKey;

    private JsonWebKey encryptionWebKey;

    private final OidcRegisteredServiceJwtCipherExecutor cipherExecutor;

    InternalJwtAccessTokenCipherExecutor(final String encryptionKey, final String signingKey,
                                         final OidcRegisteredServiceJwtCipherExecutor cipherExecutor) {
        super(encryptionKey, signingKey, StringUtils.isNotBlank(encryptionKey),
            StringUtils.isNotBlank(signingKey), 0, 0);
        this.cipherExecutor = cipherExecutor;
    }


    /**
     * Get ticket cipher executor.
     *
     * @param signingKey        the signing key
     * @param encryptionKey     the encryption key
     * @param registeredService the registered service
     * @param cipherExecutor    the cipher executor
     * @return the jwt ticket cipher executor
     */
    public static JwtTicketCipherExecutor get(final String signingKey, final String encryptionKey,
                                              final RegisteredService registeredService,
                                              final OidcRegisteredServiceJwtCipherExecutor cipherExecutor) {
        val cipher = new InternalJwtAccessTokenCipherExecutor(encryptionKey, signingKey, cipherExecutor);
        Unchecked.consumer(__ -> {
            if (EncodingUtils.isJsonWebKey(encryptionKey)) {
                val jsonWebKey = toJsonWebKey(encryptionKey, registeredService);
                cipher.setEncryptionKey(jsonWebKey.getPublicKey());
                cipher.setEncryptionWebKey(jsonWebKey);
            }
            if (EncodingUtils.isJsonWebKey(signingKey)) {
                val jsonWebKey = toJsonWebKey(signingKey, registeredService);

                /*
                 * Use the private key as the primary key to handle signing operations.
                 * The private key may also be used for validating signed objects.
                 * If the private key is not found, in the case where the keystore only contains a public
                 * key, then use the public key as the primary signing key, turning this cipher into one
                 * that can only verify signed objects, which would be useful when processing signed
                 * request objects that are sent by the RP, which has the only copy of the private key.
                 */
                cipher.setSigningKey(ObjectUtils.getIfNull(jsonWebKey.getPrivateKey(), jsonWebKey.getKey()));
                cipher.setSigningWebKey(jsonWebKey);
            }
        }).accept(cipher);

        if (EncodingUtils.isJsonWebKey(encryptionKey) || EncodingUtils.isJsonWebKey(signingKey)) {
            cipher.setEncryptionAlgorithm(KeyManagementAlgorithmIdentifiers.RSA_OAEP_256);
        }
        cipher.setCommonHeaders(CollectionUtils.wrap(
            RegisteredServiceCipherExecutor.CUSTOM_HEADER_REGISTERED_SERVICE_ID, registeredService.getId()));
        return cipher;
    }


    private Key getEncryptionKeyForDecryption(final RegisteredService registeredService) {
        val svc = (OAuthRegisteredService) registeredService;
        if (svc instanceof OidcRegisteredService) {
            val jwks = Objects.requireNonNull(cipherExecutor.getRegisteredServiceJsonWebKeystoreCache().get(
                new OidcJsonWebKeyCacheKey(svc, OidcJsonWebKeyUsage.ENCRYPTION)));
            if (jwks.isEmpty()) {
                LOGGER.warn("Service [{}] with client id [{}] is configured to encrypt tokens, yet no JSON web key is available",
                    svc.getServiceId(), svc.getClientId());
                return null;
            }
            val jsonWebKey = (PublicJsonWebKey) jwks.get().getJsonWebKeys().getFirst();
            LOGGER.debug("Found JSON web key to encrypt the token: [{}]", jsonWebKey);
            if (jsonWebKey.getPrivateKey() == null) {
                LOGGER.info("JSON web key used to encrypt the token has no associated private key, "
                            + "when operating on service [{}] with client id [{}]. Operations that deal "
                            + "with JWT encryption/decryption may not be functional, until a private "
                            + "key can be loaded for JSON web key [{}]",
                    svc.getServiceId(), svc.getClientId(), jsonWebKey.getKeyId());
                return null;
            }
            return jsonWebKey.getPrivateKey();
        }
        return null;
    }


    protected static PublicJsonWebKey toJsonWebKey(final String key, final RegisteredService registeredService) throws Exception {
        val details = EncodingUtils.parseJsonWebKey(key);
        if (details.containsKey(JsonWebKeySet.JWK_SET_MEMBER_NAME)) {
            return (PublicJsonWebKey) new JsonWebKeySet(key).getJsonWebKeys().getFirst();
        }
        return (PublicJsonWebKey) EncodingUtils.newJsonWebKey(key);
    }

    @Override
    protected byte[] sign(final byte[] value, final Key signingKey) {
        return Optional.ofNullable(this.signingWebKey)
            .map(key -> {
                val kid = key.getKeyId();
                if (StringUtils.isNotBlank(kid)) {
                    getSigningOpHeaders().put(JsonWebKey.KEY_ID_PARAMETER, kid);
                }
                val alg = StringUtils.defaultIfBlank(key.getAlgorithm(),
                    getSigningAlgorithmFor(key.getKey()));
                getSigningOpHeaders().put(JsonWebKey.ALGORITHM_PARAMETER, alg);
                return signWith(value, alg, signingKey);
            })
            .orElseGet(() -> super.sign(value, signingKey));
    }

    @Override
    protected String decode(final Serializable value, final Object[] parameters,
                            final Key encKey, final Key signingKey) {
        if (parameters.length > 0) {
            val registeredService = (RegisteredService) parameters[0];
            val decryptionKey = getEncryptionKeyForDecryption(registeredService);
            return super.decode(value, parameters, decryptionKey, signingKey);
        }
        return super.decode(value, parameters, encKey, signingKey);
    }
}
