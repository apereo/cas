package org.apereo.cas.aup;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link MongoDbAcceptableUsagePolicyRepository}.
 * Examines the principal attribute collection to determine if
 * the policy has been accepted, and if not, allows for a configurable
 * way so that user's choice can later be remembered and saved back into
 * the mongo instance.
 *
 * @author Misagh Moayyed
 * @since 5.2
 */
public class MongoDbAcceptableUsagePolicyRepository extends AbstractPrincipalAttributeAcceptableUsagePolicyRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbAcceptableUsagePolicyRepository.class);

    private static final long serialVersionUID = 1600024683199961892L;

    private final MongoTemplate mongoTemplate;
    private final String collection;

    public MongoDbAcceptableUsagePolicyRepository(final TicketRegistrySupport ticketRegistrySupport,
                                               final String aupAttributeName,
                                               final MongoTemplate mongoTemplate,
                                               final String collection) {
        super(ticketRegistrySupport, aupAttributeName);
        this.collection = collection;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public boolean submit(final RequestContext requestContext, final Credential credential) {
        try {
            final Update update = Update.update(this.aupAttributeName, Boolean.TRUE);
            final Query query = new Query(Criteria.where("username").is(credential.getId()));
            this.mongoTemplate.updateFirst(query, update, this.collection);
            return true;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
