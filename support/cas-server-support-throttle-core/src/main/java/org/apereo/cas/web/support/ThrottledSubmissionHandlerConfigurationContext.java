package org.apereo.cas.web.support;

import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.throttle.ThrottledRequestExecutor;
import org.apereo.cas.throttle.ThrottledRequestResponseHandler;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link ThrottledSubmissionHandlerConfigurationContext}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ToString
@Getter
@Setter
@SuperBuilder
public class ThrottledSubmissionHandlerConfigurationContext {
    private final AuditTrailExecutionPlan auditTrailExecutionPlan;

    private final ThrottledRequestResponseHandler throttledRequestResponseHandler;

    private final ThrottledRequestExecutor throttledRequestExecutor;

    private final ConfigurableApplicationContext applicationContext;

    private final CasConfigurationProperties casProperties;
}
