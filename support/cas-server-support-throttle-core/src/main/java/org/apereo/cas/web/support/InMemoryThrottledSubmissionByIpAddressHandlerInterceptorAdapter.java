package org.apereo.cas.web.support;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
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
@Slf4j
public class InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter extends AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter {

    public InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter(final int failureThreshold,
                                                                           final int failureRangeInSeconds,
                                                                           final String usernameParameter,
                                                                           final String authenticationFailureCode,
                                                                           final AuditTrailExecutionPlan auditTrailExecutionPlan,
                                                                           final String applicationCode,
                                                                           final ConcurrentMap map) {
        super(failureThreshold, failureRangeInSeconds, usernameParameter,
            authenticationFailureCode, auditTrailExecutionPlan, applicationCode, map);
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
