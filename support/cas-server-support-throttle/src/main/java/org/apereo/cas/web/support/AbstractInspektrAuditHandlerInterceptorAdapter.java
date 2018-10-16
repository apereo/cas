package org.apereo.cas.web.support;

import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.throttle.ThrottledRequestResponseHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link AbstractInspektrAuditHandlerInterceptorAdapter}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public abstract class AbstractInspektrAuditHandlerInterceptorAdapter extends AbstractThrottledSubmissionHandlerInterceptorAdapter {
    public AbstractInspektrAuditHandlerInterceptorAdapter(final int failureThreshold, final int failureRangeInSeconds,
                                                          final String usernameParameter, final String authenticationFailureCode,
                                                          final AuditTrailExecutionPlan auditTrailManager,
                                                          final String applicationCode, final ThrottledRequestResponseHandler throttledRequestResponseHandler) {
        super(failureThreshold, failureRangeInSeconds, usernameParameter,
            authenticationFailureCode, auditTrailManager, applicationCode, throttledRequestResponseHandler);
    }

    @Override
    protected void recordThrottle(final HttpServletRequest request) {
        super.recordThrottle(request);
        recordAuditAction(request, ACTION_THROTTLED_LOGIN_ATTEMPT);
    }
}
