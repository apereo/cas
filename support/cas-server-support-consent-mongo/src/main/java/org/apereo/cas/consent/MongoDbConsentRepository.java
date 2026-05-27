package org.apereo.cas.consent;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.services.RegisteredService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/**
 * This is {@link MongoDbConsentRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredArgsConstructor
public class MongoDbConsentRepository extends BaseConsentRepository {
    @Serial
    private static final long serialVersionUID = 7734163279139907616L;

    private final MongoOperations mongoTemplate;

    private final String collectionName;

    @Override
    public @Nullable ConsentDecision findConsentDecision(final Service service,
                                                         final RegisteredService registeredService,
                                                         final Authentication authentication) {
        val query = new Query(Criteria.where("service").is(service.getId())
            .and("principal").is(authentication.getPrincipal().getId()));
        return mongoTemplate.findOne(query, ConsentDecision.class, collectionName);
    }

    @Override
    public Collection<? extends ConsentDecision> findConsentDecisions(final String principal) {
        val query = new Query(Criteria.where("principal").is(principal));
        return mongoTemplate.find(query, ConsentDecision.class, collectionName);
    }

    @Override
    public Collection<? extends ConsentDecision> findConsentDecisions() {
        return mongoTemplate.findAll(ConsentDecision.class, collectionName);
    }

    @Override
    public ConsentDecision storeConsentDecision(final ConsentDecision decision) {
        return mongoTemplate.save(decision, collectionName);
    }

    @Override
    public boolean deleteConsentDecision(final long decisionId, final String principal) {
        val query = new Query(Criteria.where("id").is(decisionId).and("principal").is(principal));
        val result = mongoTemplate.remove(query, ConsentDecision.class, collectionName);
        return result.getDeletedCount() > 0;
    }

    @Override
    public boolean deleteConsentDecisions(final String principal) {
        val query = new Query(Criteria.where("principal").is(principal));
        val result = mongoTemplate.remove(query, ConsentDecision.class, collectionName);
        return result.getDeletedCount() > 0;
    }

    @Override
    public void deleteAll() {
        val query = new Query(Criteria.where("principal").exists(true));
        mongoTemplate.remove(query, ConsentDecision.class, collectionName);
    }

    /**
     * Create repository instance from properties.
     *
     * @param casSslContext the cas ssl context
     * @param casProperties the cas properties
     * @return the mongo db consent repository
     */
    public static MongoDbConsentRepository from(
        final CasSSLContext casSslContext,
        final CasConfigurationProperties casProperties) {
        val mongo = casProperties.getConsent().getMongo();
        val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
        val mongoTemplate = factory.buildMongoTemplate(mongo);
        MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
        return new MongoDbConsentRepository(mongoTemplate, mongo.getCollection());
    }
}
