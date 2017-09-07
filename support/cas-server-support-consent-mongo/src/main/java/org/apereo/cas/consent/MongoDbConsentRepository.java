package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collection;

/**
 * This is {@link MongoDbConsentRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class MongoDbConsentRepository implements ConsentRepository {
    private static final long serialVersionUID = 7734391779139907616L;
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbConsentRepository.class);

    private final String collectionName;
    private final MongoTemplate mongoTemplate;

    public MongoDbConsentRepository(final MongoTemplate mongoTemplate, final String collectionName, final boolean dropCollection) {
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;

        if (dropCollection) {
            LOGGER.debug("Dropping database collection: [{}]", this.collectionName);
            this.mongoTemplate.dropCollection(this.collectionName);
        }

        if (!this.mongoTemplate.collectionExists(this.collectionName)) {
            LOGGER.debug("Creating database collection: [{}]", this.collectionName);
            this.mongoTemplate.createCollection(this.collectionName);
        }
    }

    @Override
    public ConsentDecision findConsentDecision(final Service service,
                                               final RegisteredService registeredService,
                                               final Authentication authentication) {
        final Query query = new Query(Criteria.where("service").is(service.getId()).and("principal").is(authentication.getPrincipal().getId()));
        return this.mongoTemplate.findOne(query, ConsentDecision.class, this.collectionName);
    }

    @Override
    public Collection<ConsentDecision> findConsentDecisions(final String principal) {
        final Query query = new Query(Criteria.where("principal").is(principal));
        return this.mongoTemplate.find(query, ConsentDecision.class, this.collectionName);
    }

    @Override
    public Collection<ConsentDecision> findConsentDecisions() {
        return this.mongoTemplate.findAll(ConsentDecision.class, this.collectionName);
    }

    @Override
    public boolean storeConsentDecision(final ConsentDecision decision) {
        this.mongoTemplate.save(decision, this.collectionName);
        return true;
    }
}
