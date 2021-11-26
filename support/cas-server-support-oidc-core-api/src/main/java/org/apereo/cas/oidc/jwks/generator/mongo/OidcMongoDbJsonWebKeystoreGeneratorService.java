package org.apereo.cas.oidc.jwks.generator.mongo;

import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreEntity;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Optional;

/**
 * This is {@link OidcMongoDbJsonWebKeystoreGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
public class OidcMongoDbJsonWebKeystoreGeneratorService implements OidcJsonWebKeystoreGeneratorService {
    private final MongoTemplate mongoTemplate;

    private final OidcProperties oidcProperties;

    @Override
    public Resource generate() throws Exception {
        return find()
            .orElseGet(Unchecked.supplier(() -> {
                val jsonWebKeySet = OidcJsonWebKeystoreGeneratorService.generateJsonWebKeySet(oidcProperties);
                return OidcJsonWebKeystoreGeneratorService.toResource(store(jsonWebKeySet));
            }));
    }

    @Override
    public JsonWebKeySet store(final JsonWebKeySet jsonWebKeySet) throws Exception {
        val issuer = oidcProperties.getCore().getIssuer();
        val collectionName = oidcProperties.getJwks().getMongo().getCollection();
        val result = mongoTemplate.findById(issuer, OidcJsonWebKeystoreEntity.class, collectionName);
        val json = jsonWebKeySet.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
        Optional.ofNullable(result)
            .ifPresentOrElse(entity -> {
                val update = Update.update("data", json);
                val query = new Query(Criteria.where("issuer").is(entity.getIssuer()));
                mongoTemplate.updateFirst(query, update, collectionName);
            }, () -> {
                val entity = new OidcJsonWebKeystoreEntity(issuer, json);
                mongoTemplate.insert(entity, collectionName);
            });
        return jsonWebKeySet;
    }

    @Override
    public Optional<Resource> find() throws Exception {
        val issuer = oidcProperties.getCore().getIssuer();
        val entity = mongoTemplate.findById(issuer, OidcJsonWebKeystoreEntity.class,
            oidcProperties.getJwks().getMongo().getCollection());
        return Optional.ofNullable(entity)
            .map(Unchecked.function(jwks ->
                OidcJsonWebKeystoreGeneratorService.toResource(new JsonWebKeySet(jwks.getData()))));
    }
}
