package org.apereo.cas.web.support;

import org.apereo.cas.audit.AuditTrailExecutionPlan;

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

    public MongoDbThrottledSubmissionHandlerInterceptorAdapter(final int failureThreshold,
                                                               final int failureRangeInSeconds,
                                                               final String usernameParameter,
                                                               final AuditTrailExecutionPlan auditTrailExecutionPlan,
                                                               final MongoTemplate mongoTemplate,
                                                               final String authenticationFailureCode,
                                                               final String applicationCode, final String collectionName) {
        super(failureThreshold, failureRangeInSeconds, usernameParameter,
            authenticationFailureCode, auditTrailExecutionPlan, applicationCode);
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
                .and("actionPerformed").is(getAuthenticationFailureCode())
                .and("applicationCode").is(getApplicationCode())
                .and("whenActionWasPerformed").gte(getFailureInRangeCutOffDate()));

        query.with(new Sort(Sort.Direction.DESC, "whenActionWasPerformed"));
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
