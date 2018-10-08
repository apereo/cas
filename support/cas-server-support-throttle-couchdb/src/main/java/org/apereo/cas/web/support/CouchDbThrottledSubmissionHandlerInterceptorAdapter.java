package org.apereo.cas.web.support;

import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.couchdb.audit.AuditActionContextCouchDbRepository;
import org.apereo.cas.throttle.ThrottledRequestResponseHandler;

import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.common.web.ClientInfoHolder;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;

/**
 * This is {@link CouchDbThrottledSubmissionHandlerInterceptorAdapter}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public class CouchDbThrottledSubmissionHandlerInterceptorAdapter extends AbstractInspektrAuditHandlerInterceptorAdapter {

    private static final String NAME = "CouchDbThrottle";

    private AuditActionContextCouchDbRepository repository;

    public CouchDbThrottledSubmissionHandlerInterceptorAdapter(final int failureThreshold,
                                                               final int failureRangeInSeconds,
                                                               final String usernameParameter,
                                                               final String authenticationFailureCode,
                                                               final AuditTrailExecutionPlan auditTrailManager,
                                                               final String applicationCode,
                                                               final AuditActionContextCouchDbRepository repository,
                                                               final ThrottledRequestResponseHandler throttledRequestResponseHandler) {
        super(failureThreshold, failureRangeInSeconds, usernameParameter, authenticationFailureCode, auditTrailManager, applicationCode, throttledRequestResponseHandler);
        this.repository = repository;
    }

    @Override
    public boolean exceedsThreshold(final HttpServletRequest request) {
        val clientInfo = ClientInfoHolder.getClientInfo();
        val remoteAddress = clientInfo.getClientIpAddress();

        val failures = repository.findByThrottleParams(remoteAddress,
            getUsernameParameterFromRequest(request),
            getAuthenticationFailureCode(),
            getApplicationCode(),
            LocalDateTime.now(ZoneOffset.UTC).minusSeconds(getFailureRangeInSeconds()))
            .stream().map(AuditActionContext::getWhenActionWasPerformed).collect(Collectors.toList());

        return calculateFailureThresholdRateAndCompare(failures);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
