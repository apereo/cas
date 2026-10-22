package org.apereo.cas.aup;

import module java.base;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.mongodb.core.MongoOperations;
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
    @Serial
    private static final long serialVersionUID = 1600024683199961892L;

    private final MongoOperations mongoTemplate;

    public MongoDbAcceptableUsagePolicyRepository(final AcceptableUsagePolicyProperties aupProperties,
                                                  final MongoOperations mongoTemplate) {
        super(aupProperties);
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Verify the policy through the principal attributes first and, failing that, through the decision kept in
     * the collection, so an acceptance recorded at an earlier login is honored.
     *
     * @param requestContext the request context
     * @return the policy status
     * @throws Throwable the throwable
     */
    @Override
    public AcceptableUsagePolicyStatus verify(final RequestContext requestContext) throws Throwable {
        val status = super.verify(requestContext);
        if (status.isDenied()) {
            val principal = status.getPrincipal();
            val query = new Query(Criteria.where("username").is(principal.getId())
                .and(aupProperties.getCore().getAupAttributeName()).is(Boolean.TRUE));
            if (mongoTemplate.exists(query, aupProperties.getMongo().getCollection())) {
                return AcceptableUsagePolicyStatus.accepted(principal);
            }
        }
        return status;
    }

    /**
     * Record the acceptance for the principal, creating the principal's document when there is none yet.
     *
     * @param requestContext the request context
     * @return true if the decision was recorded
     */
    @Override
    public boolean submit(final RequestContext requestContext) {
        try {
            val principal = WebUtils.getAuthentication(requestContext).getPrincipal();
            val update = Update.update(aupProperties.getCore().getAupAttributeName(), Boolean.TRUE);
            val query = new Query(Criteria.where("username").is(principal.getId()));
            this.mongoTemplate.upsert(query, update, aupProperties.getMongo().getCollection());
            return true;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }
}
