package org.apereo.cas.web.support;

import org.apereo.cas.couchdb.audit.AuditActionContextCouchDbRepository;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;

/**
 * This is {@link CouchDbThrottledSubmissionHandlerInterceptorAdapter}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 * @deprecated Since 7
 */
@Deprecated(since = "7.0.0")
public class CouchDbThrottledSubmissionHandlerInterceptorAdapter extends AbstractInspektrAuditHandlerInterceptorAdapter {

    private final AuditActionContextCouchDbRepository repository;

    public CouchDbThrottledSubmissionHandlerInterceptorAdapter(final ThrottledSubmissionHandlerConfigurationContext configurationContext,
                                                               final AuditActionContextCouchDbRepository repository) {
        super(configurationContext);
        this.repository = repository;
    }

    @Override
    public boolean exceedsThreshold(final HttpServletRequest request) {
        val clientInfo = ClientInfoHolder.getClientInfo();
        val remoteAddress = clientInfo.getClientIpAddress();
        val throttle = getConfigurationContext().getCasProperties().getAuthn().getThrottle();

        val username = getUsernameParameterFromRequest(request);
        val failures = repository.findByThrottleParams(remoteAddress,
                username,
                throttle.getFailure().getCode(),
                throttle.getCore().getAppCode(),
                LocalDateTime.now(ZoneOffset.UTC).minusSeconds(throttle.getFailure().getRangeSeconds()))
            .stream()
            .map(this::toThrottledSubmission)
            .collect(Collectors.toList());

        return calculateFailureThresholdRateAndCompare(failures);
    }

    @Override
    public String getName() {
        return "CouchDbThrottle";
    }
}
