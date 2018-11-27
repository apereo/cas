package org.apereo.cas.web.support;

import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.throttle.ThrottledRequestExecutor;
import org.apereo.cas.throttle.ThrottledRequestResponseHandler;

import org.apereo.inspektr.common.web.ClientInfoHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ConcurrentMap;

/**
 * Throttles access attempts for failed logins by IP Address. This stores the attempts in memory.
 * This is not good for a clustered environment unless the intended behavior is that this blocking is per-machine.
 *
 * @author Scott Battaglia
 * @since 3.3.5
 */
public class InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter extends AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter {

    public InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter(final int failureThreshold,
                                                                           final int failureRangeInSeconds,
                                                                           final String usernameParameter,
                                                                           final String authenticationFailureCode,
                                                                           final AuditTrailExecutionPlan auditTrailExecutionPlan,
                                                                           final String applicationCode,
                                                                           final ThrottledRequestResponseHandler throttledRequestResponseHandler,
                                                                           final ConcurrentMap map,
                                                                           final ThrottledRequestExecutor throttledRequestExecutor) {
        super(failureThreshold, failureRangeInSeconds, usernameParameter,
            authenticationFailureCode, auditTrailExecutionPlan, applicationCode,
            throttledRequestResponseHandler, map, throttledRequestExecutor);
    }

    @Override
    public String constructKey(final HttpServletRequest request) {
        return ClientInfoHolder.getClientInfo().getClientIpAddress();
    }

    @Override
    public String getName() {
        return "inMemoryIpAddressThrottle";
    }
}
