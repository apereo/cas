package org.apereo.cas.aup;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
@Slf4j
public class MongoDbAcceptableUsagePolicyRepository extends BaseAcceptableUsagePolicyRepository {
    private static final long serialVersionUID = 1600024683199961892L;

    private final transient MongoTemplate mongoTemplate;

    public MongoDbAcceptableUsagePolicyRepository(final TicketRegistrySupport ticketRegistrySupport,
                                                  final AcceptableUsagePolicyProperties aupProperties,
                                                  final MongoTemplate mongoTemplate) {
        super(ticketRegistrySupport, aupProperties);
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public boolean submit(final RequestContext requestContext, final Credential credential) {
        try {
            val update = Update.update(aupProperties.getAupAttributeName(), Boolean.TRUE);
            val query = new Query(Criteria.where("username").is(credential.getId()));
            this.mongoTemplate.updateFirst(query, update, aupProperties.getMongo().getCollection());
            return true;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
