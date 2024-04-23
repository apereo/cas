package org.apereo.cas.web.support;

import org.apereo.cas.audit.RedisAuditTrailManager;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.throttle.AbstractInspektrAuditHandlerInterceptorAdapter;
import org.apereo.cas.throttle.ThrottledSubmissionHandlerConfigurationContext;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.data.redis.core.BoundValueOperations;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Works in conjunction with a redis database to
 * block attempts to dictionary attack users.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class RedisThrottledSubmissionHandlerInterceptorAdapter extends AbstractInspektrAuditHandlerInterceptorAdapter {
    private final CasRedisTemplate<String, Object> redisTemplate;

    private final long scanCount;

    public RedisThrottledSubmissionHandlerInterceptorAdapter(
        final ThrottledSubmissionHandlerConfigurationContext configurationContext,
        final CasRedisTemplate<String, Object> redisTemplate,
        final long scanCount) {
        super(configurationContext);
        this.redisTemplate = redisTemplate;
        this.scanCount = scanCount;
    }

    @Override
    public boolean exceedsThreshold(final HttpServletRequest request) {
        val clientInfo = ClientInfoHolder.getClientInfo();
        val remoteAddress = clientInfo.getClientIpAddress();
        val throttle = getConfigurationContext().getCasProperties().getAuthn().getThrottle();
        try (val keys = redisTemplate.scan(RedisAuditTrailManager.CAS_AUDIT_CONTEXT_PREFIX + '*', this.scanCount)) {
            val username = getUsernameParameterFromRequest(request);
            val failures = keys
                .map((Function<String, BoundValueOperations>) redisTemplate::boundValueOps)
                .map(BoundValueOperations::get)
                .map(AuditActionContext.class::cast)
                .filter(audit ->
                    audit.getPrincipal().equalsIgnoreCase(username)
                        && audit.getClientInfo().getClientIpAddress().equalsIgnoreCase(remoteAddress)
                        && audit.getActionPerformed().equalsIgnoreCase(throttle.getFailure().getCode())
                        && audit.getApplicationCode().equalsIgnoreCase(throttle.getCore().getAppCode())
                        && audit.getWhenActionWasPerformed().isAfter(getFailureInRangeCutOffDate()))
                .sorted(Comparator.comparing(AuditActionContext::getWhenActionWasPerformed).reversed())
                .limit(2)
                .map(this::toThrottledSubmission)
                .collect(Collectors.toList());
            return calculateFailureThresholdRateAndCompare(failures);
        }
    }

    @Override
    public String getName() {
        return "RedisThrottle";
    }
}
