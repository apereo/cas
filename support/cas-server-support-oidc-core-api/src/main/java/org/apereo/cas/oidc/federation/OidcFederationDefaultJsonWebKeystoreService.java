package org.apereo.cas.oidc.federation;

import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreUtils;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatementClaimsSet;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link OidcFederationDefaultJsonWebKeystoreService}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Slf4j
public class OidcFederationDefaultJsonWebKeystoreService implements OidcFederationJsonWebKeystoreService {
    private JsonWebKey jsonWebKey;

    public OidcFederationDefaultJsonWebKeystoreService(
        final OidcProperties oidcProperties) throws Exception {
        val jwksFile = SpringExpressionLanguageValueResolver.getInstance()
            .resolve(oidcProperties.getFederation().getJwksFile());
        val resource = ResourceUtils.getRawResourceFrom(jwksFile);
        try {
            if (ResourceUtils.doesResourceExist(jwksFile)) {
                LOGGER.debug("Loading OpenID Connect Federation JSON web key from [{}]", resource);
                val jwks = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
                val jsonWebKeys = new JsonWebKeySet(jwks).getJsonWebKeys();
                if (!jsonWebKeys.isEmpty()) {
                    jsonWebKey = jsonWebKeys.getFirst();
                    LOGGER.debug("Loaded OpenID Connect Federation JSON web key from [{}]", jsonWebKey.getKeyId());
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            if (jsonWebKey == null) {
                LOGGER.debug("JSON web key for OpenID Connect Federation is not found. Generating a new one");
                jsonWebKey = OidcJsonWebKeyStoreUtils.generateJsonWebKey(
                    oidcProperties.getJwks().getCore().getJwksType(),
                    oidcProperties.getJwks().getCore().getJwksKeySize(),
                    OidcJsonWebKeyUsage.SIGNING);
                if (!ResourceUtils.doesResourceExist(jwksFile)) {
                    val jsonWebKeySet = new JsonWebKeySet(jsonWebKey);
                    val json = jsonWebKeySet.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
                    LOGGER.debug("Writing OpenID Connect Federation JSON web key to [{}]", resource);
                    FileUtils.write(resource.getFile(), json, StandardCharsets.UTF_8);
                }
            }
        }
    }

    @Override
    public JWKSet toJWKSet() throws Exception {
        val jwk = JWK.parse(jsonWebKey.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE));
        return new JWKSet(jwk.toPublicJWK());
    }

    @Override
    public EntityStatement signEntityStatement(final EntityStatementClaimsSet claims) throws Exception {
        val jwk = JWK.parse(jsonWebKey.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE));
        return EntityStatement.sign(claims, jwk);
    }
}
