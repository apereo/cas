package org.apereo.cas.web.support;

import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.throttle.ThrottledRequestExecutor;
import org.apereo.cas.throttle.ThrottledRequestResponseHandler;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link ThrottledSubmissionHandlerConfigurationContext}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ToString
@Getter
@Setter
@Builder
public class ThrottledSubmissionHandlerConfigurationContext {
    private final int failureThreshold;

    private final int failureRangeInSeconds;

    private final String usernameParameter;
    private final String authenticationFailureCode;

    private final AuditTrailExecutionPlan auditTrailExecutionPlan;

    private final String applicationCode;

    private final ThrottledRequestResponseHandler throttledRequestResponseHandler;

    private final ThrottledRequestExecutor throttledRequestExecutor;
}
