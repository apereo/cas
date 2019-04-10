package org.apereo.cas.web.support;

import org.apereo.cas.couchdb.audit.AuditActionContextCouchDbRepository;

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

        val failures = repository.findByThrottleParams(remoteAddress,
            getUsernameParameterFromRequest(request),
            getConfigurationContext().getAuthenticationFailureCode(),
            getConfigurationContext().getApplicationCode(),
            LocalDateTime.now(ZoneOffset.UTC).minusSeconds(getConfigurationContext().getFailureRangeInSeconds()))
            .stream().map(AuditActionContext::getWhenActionWasPerformed).collect(Collectors.toList());

        return calculateFailureThresholdRateAndCompare(failures);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
