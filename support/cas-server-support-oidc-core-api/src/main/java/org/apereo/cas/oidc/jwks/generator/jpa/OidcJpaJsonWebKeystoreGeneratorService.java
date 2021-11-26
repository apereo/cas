package org.apereo.cas.oidc.jwks.generator.jpa;

import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreEntity;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

/**
 * This is {@link OidcJpaJsonWebKeystoreGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
@EnableTransactionManagement
@Transactional(transactionManager = "transactionManagerOidcJwks")
public class OidcJpaJsonWebKeystoreGeneratorService implements OidcJsonWebKeystoreGeneratorService {
    private final OidcProperties oidcProperties;

    private final TransactionTemplate transactionTemplate;

    @PersistenceContext(unitName = "oidcJwksEntityManagerFactory")
    private EntityManager entityManager;

    @Override
    public Optional<Resource> find() {
        val issuer = oidcProperties.getCore().getIssuer();
        return Optional.ofNullable(entityManager.find(OidcJsonWebKeystoreEntity.class, issuer))
            .map(Unchecked.function(jwks -> OidcJsonWebKeystoreGeneratorService.toResource(new JsonWebKeySet(jwks.getData()))));
    }

    @Override
    public Resource generate() {
        val issuer = oidcProperties.getCore().getIssuer();
        return Optional.ofNullable(entityManager.find(OidcJsonWebKeystoreEntity.class, issuer))
            .map(Unchecked.function(jwks -> OidcJsonWebKeystoreGeneratorService.toResource(new JsonWebKeySet(jwks.getData()))))
            .orElseGet(Unchecked.supplier(() -> {
                val jsonWebKeySet = OidcJsonWebKeystoreGeneratorService.generateJsonWebKeySet(oidcProperties);
                return OidcJsonWebKeystoreGeneratorService.toResource(store(jsonWebKeySet));
            }));
    }

    @Override
    public JsonWebKeySet store(final JsonWebKeySet jsonWebKeySet) throws Exception {
        val issuer = oidcProperties.getCore().getIssuer();
        return transactionTemplate.execute(status -> {
            val result = jsonWebKeySet.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
            Optional.ofNullable(entityManager.find(OidcJsonWebKeystoreEntity.class, issuer))
                .ifPresentOrElse(entity -> {
                    entity.setData(result);
                    entityManager.merge(entity);
                }, () -> {
                    val entity = new OidcJsonWebKeystoreEntity(issuer, result);
                    entityManager.persist(entity);
                });
            return jsonWebKeySet;
        });
    }
}
