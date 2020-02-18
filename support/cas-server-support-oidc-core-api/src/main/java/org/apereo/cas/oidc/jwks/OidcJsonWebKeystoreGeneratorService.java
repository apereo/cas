package org.apereo.cas.oidc.jwks;

import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.jose4j.jwk.EcJwkGenerator;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.keys.EllipticCurves;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * This is {@link OidcJsonWebKeystoreGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcJsonWebKeystoreGeneratorService {
    private static final int JWK_EC_P384_SIZE = 384;

    private static final int JWK_EC_P512_SIZE = 512;

    private final OidcProperties oidcProperties;

    /**
     * Generate.
     */
    @SneakyThrows
    public void generate() {
        val resolve = SpringExpressionLanguageValueResolver.getInstance().resolve(oidcProperties.getJwksFile());
        val resource = ResourceUtils.getRawResourceFrom(resolve);
        generate(resource);
    }

    /**
     * Generate.
     *
     * @param file the file
     */
    @SneakyThrows
    public void generate(final Resource file) {
        if (!ResourceUtils.doesResourceExist(file)) {
            val jwk = generateJsonWebKey();
            val jsonWebKeySet = new JsonWebKeySet(jwk);
            val data = jsonWebKeySet.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
            val location = file.getFile();
            FileUtils.write(location, data, StandardCharsets.UTF_8);
            LOGGER.debug("Generated JSON web keystore at [{}]", location);
        } else {
            LOGGER.debug("Located JSON web keystore at [{}]", file);
        }
    }

    /**
     * Generate json web key public json web key.
     *
     * @return the public json web key
     */
    @SneakyThrows
    public PublicJsonWebKey generateJsonWebKey() {
        switch (oidcProperties.getJwksType().toLowerCase()) {
            case "ec":
                if (oidcProperties.getJwksKeySize() == JWK_EC_P384_SIZE) {
                    val jwk = EcJwkGenerator.generateJwk(EllipticCurves.P384);
                    jwk.setKeyId(UUID.randomUUID().toString());
                    jwk.setAlgorithm(AlgorithmIdentifiers.ECDSA_USING_P384_CURVE_AND_SHA384);
                    return jwk;
                }
                if (oidcProperties.getJwksKeySize() == JWK_EC_P512_SIZE) {
                    val jwk = EcJwkGenerator.generateJwk(EllipticCurves.P521);
                    jwk.setKeyId(UUID.randomUUID().toString());
                    jwk.setAlgorithm(AlgorithmIdentifiers.ECDSA_USING_P521_CURVE_AND_SHA512);
                    return jwk;
                }
                val jwk = EcJwkGenerator.generateJwk(EllipticCurves.P256);
                jwk.setKeyId(UUID.randomUUID().toString());
                jwk.setAlgorithm(AlgorithmIdentifiers.ECDSA_USING_P521_CURVE_AND_SHA512);
                return jwk;

            case "rsa":
            default:
                return RsaJwkGenerator.generateJwk(oidcProperties.getJwksKeySize());
        }
    }
}
