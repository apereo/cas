package org.apereo.cas.web.support;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

/**
 * Works in conjunction with a Mongo database to block attempts to dictionary attack users.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class MongoDbThrottledSubmissionHandlerInterceptorAdapter extends AbstractInspektrAuditHandlerInterceptorAdapter {
    private final transient MongoTemplate mongoTemplate;
    private final String collectionName;

    public MongoDbThrottledSubmissionHandlerInterceptorAdapter(final ThrottledSubmissionHandlerConfigurationContext configurationContext,
                                                               final MongoTemplate mongoTemplate,
                                                               final String collectionName) {
        super(configurationContext);
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
    }

    @Override
    public boolean exceedsThreshold(final HttpServletRequest request) {
        val clientInfo = ClientInfoHolder.getClientInfo();
        val remoteAddress = clientInfo.getClientIpAddress();

        val query = new Query()
            .addCriteria(Criteria.where("clientIpAddress").is(remoteAddress)
                .and("principal").is(getUsernameParameterFromRequest(request))
                .and("actionPerformed").is(getConfigurationContext().getAuthenticationFailureCode())
                .and("applicationCode").is(getConfigurationContext().getApplicationCode())
                .and("whenActionWasPerformed").gte(getFailureInRangeCutOffDate()));
        query.with(Sort.by(Sort.Direction.DESC, "whenActionWasPerformed"));
        query.limit(2);
        query.fields().include("whenActionWasPerformed");

        LOGGER.debug("Executing MongoDb throttling query [{}]", query.toString());
        val failures = this.mongoTemplate.find(query, AuditActionContext.class, this.collectionName)
            .stream()
            .map(AuditActionContext::getWhenActionWasPerformed)
            .collect(Collectors.toList());
        return calculateFailureThresholdRateAndCompare(failures);
    }

    @Override
    public String getName() {
        return "MongoDbThrottle";
    }
}
