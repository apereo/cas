package org.apereo.cas.oidc.jwks.generator.jpa;

import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.configuration.support.JpaPersistenceUnitProvider;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreEntity;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;
import lombok.Getter;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;

/**
 * This is {@link OidcJpaJsonWebKeystoreGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@EnableTransactionManagement(proxyTargetClass = false)
@Order(Ordered.HIGHEST_PRECEDENCE)
@Transactional(transactionManager = "transactionManagerOidcJwks")
public class OidcJpaJsonWebKeystoreGeneratorService implements OidcJsonWebKeystoreGeneratorService, JpaPersistenceUnitProvider {
    /**
     * Persistence unit name.
     */
    public static final String PERSISTENCE_UNIT_NAME = "jpaOidcJwksContext";

    private final OidcProperties oidcProperties;

    private final TransactionOperations transactionTemplate;

    @Getter
    private final ConfigurableApplicationContext applicationContext;

    @PersistenceContext(unitName = PERSISTENCE_UNIT_NAME)
    @Getter
    private EntityManager entityManager;

    public OidcJpaJsonWebKeystoreGeneratorService(final OidcProperties oidcProperties,
                                                  final TransactionOperations transactionTemplate,
                                                  final ConfigurableApplicationContext applicationContext) {
        this.oidcProperties = oidcProperties;
        this.transactionTemplate = transactionTemplate;
        this.applicationContext = applicationContext;
        this.entityManager = recreateEntityManagerIfNecessary(PERSISTENCE_UNIT_NAME);
    }

    @Override
    public Optional<Resource> find() {
        val issuer = oidcProperties.getCore().getIssuer();
        return Optional.ofNullable(entityManager.find(OidcJsonWebKeystoreEntity.class, issuer))
            .map(Unchecked.function(jwks -> OidcJsonWebKeystoreGeneratorService.toResource(new JsonWebKeySet(jwks.getData()))));
    }

    @Override
    public Resource generate() throws Throwable {
        val issuer = oidcProperties.getCore().getIssuer();
        return Optional.ofNullable(entityManager.find(OidcJsonWebKeystoreEntity.class, issuer))
            .map(Unchecked.function(jwks -> OidcJsonWebKeystoreGeneratorService.toResource(new JsonWebKeySet(jwks.getData()))))
            .orElseGet(Unchecked.supplier(() -> {
                val jsonWebKeySet = OidcJsonWebKeystoreGeneratorService.generateJsonWebKeySet(oidcProperties);
                return OidcJsonWebKeystoreGeneratorService.toResource(store(jsonWebKeySet));
            }));
    }

    @Override
    public JsonWebKeySet store(final JsonWebKeySet jsonWebKeySet) throws Throwable {
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
