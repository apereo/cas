package org.apereo.cas.oidc.jwks.generator.mongo;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreEntity;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoOperations;

/**
 * This is {@link OidcMongoDbJsonWebKeystoreGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OidcMongoDbJsonWebKeystoreGeneratorService implements OidcJsonWebKeystoreGeneratorService {
    private final MongoOperations mongoTemplate;

    private final OidcProperties oidcProperties;

    @Override
    public Resource generate() {
        return find()
            .orElseGet(Unchecked.supplier(() -> {
                val jsonWebKeySet = OidcJsonWebKeystoreGeneratorService.generateJsonWebKeySet(oidcProperties);
                return OidcJsonWebKeystoreGeneratorService.toResource(store(jsonWebKeySet));
            }));
    }

    @Override
    public JsonWebKeySet store(final JsonWebKeySet jsonWebKeySet) {
        val issuer = oidcProperties.getCore().getIssuer();
        val collectionName = oidcProperties.getJwks().getMongo().getCollection();
        val json = jsonWebKeySet.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
        val entity = new OidcJsonWebKeystoreEntity(issuer, json);
        mongoTemplate.save(entity, collectionName);
        return jsonWebKeySet;
    }

    @Override
    public Optional<Resource> find() {
        val issuer = oidcProperties.getCore().getIssuer();
        val entity = mongoTemplate.findById(issuer, OidcJsonWebKeystoreEntity.class,
            oidcProperties.getJwks().getMongo().getCollection());
        return Optional.ofNullable(entity)
            .map(Unchecked.function(jwks ->
                OidcJsonWebKeystoreGeneratorService.toResource(new JsonWebKeySet(jwks.getData()))));
    }
}
