package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

import lombok.RequiredArgsConstructor;
import lombok.val;
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
@RequiredArgsConstructor
public class MongoDbConsentRepository implements ConsentRepository {
    private static final long serialVersionUID = 7734163279139907616L;

    private final transient MongoTemplate mongoTemplate;
    private final String collectionName;

    @Override
    public ConsentDecision findConsentDecision(final Service service,
                                               final RegisteredService registeredService,
                                               final Authentication authentication) {
        val query = new Query(Criteria.where("service").is(service.getId()).and("principal").is(authentication.getPrincipal().getId()));
        return this.mongoTemplate.findOne(query, ConsentDecision.class, this.collectionName);
    }

    @Override
    public Collection<? extends ConsentDecision> findConsentDecisions(final String principal) {
        val query = new Query(Criteria.where("principal").is(principal));
        return this.mongoTemplate.find(query, ConsentDecision.class, this.collectionName);
    }

    @Override
    public Collection<? extends ConsentDecision> findConsentDecisions() {
        return this.mongoTemplate.findAll(ConsentDecision.class, this.collectionName);
    }

    @Override
    public boolean storeConsentDecision(final ConsentDecision decision) {
        this.mongoTemplate.save(decision, this.collectionName);
        return true;
    }

    @Override
    public boolean deleteConsentDecision(final long decisionId, final String principal) {
        val query = new Query(Criteria.where("id").is(decisionId).and("principal").is(principal));
        val result = this.mongoTemplate.remove(query, ConsentDecision.class, this.collectionName);
        return result.getDeletedCount() > 0;
    }
}
