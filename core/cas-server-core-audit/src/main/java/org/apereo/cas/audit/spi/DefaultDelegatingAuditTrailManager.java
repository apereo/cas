package org.apereo.cas.audit.spi;


import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apereo.cas.support.events.audit.CasAuditActionContextRecordedEvent;
import org.apereo.cas.util.ISOStandardDateFormat;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link DefaultDelegatingAuditTrailManager}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultDelegatingAuditTrailManager implements DelegatingAuditTrailManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDelegatingAuditTrailManager.class);

    private static final int INITIAL_CACHE_SIZE = 50;
    private static final long MAX_CACHE_SIZE = 1_000_000;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private final AuditTrailManager manager;
    private final LoadingCache<String, AuditActionContext> storage;

    private int expirationDuration = 2;
    private TimeUnit expirationTimeUnit = TimeUnit.HOURS;

    public DefaultDelegatingAuditTrailManager(final AuditTrailManager manager) {
        this.manager = manager;
        this.storage = Caffeine.newBuilder()
            .initialCapacity(INITIAL_CACHE_SIZE)
            .maximumSize(MAX_CACHE_SIZE)
            .recordStats()
            .expireAfterWrite(this.expirationDuration, this.expirationTimeUnit)
            .build(s -> {
                LOGGER.error("Load operation of the audit cache is not supported.");
                return null;
            });
    }

    @Override
    public void record(final AuditActionContext auditActionContext) {
        this.manager.record(auditActionContext);
        final String key = new StringBuilder(auditActionContext.getPrincipal())
            .append("@").append(auditActionContext.getActionPerformed())
            .append("@").append(auditActionContext.getResourceOperatedUpon())
            .append("@").append(ISOStandardDateFormat.getInstance().format(auditActionContext.getWhenActionWasPerformed()))
            .toString();
        this.storage.put(key, auditActionContext);
        if (this.eventPublisher != null) {
            this.eventPublisher.publishEvent(new CasAuditActionContextRecordedEvent(this, auditActionContext));
        }
    }

    @Override
    public Set<AuditActionContext> get() {
        return new HashSet<>(this.storage.asMap().values());
    }

    public void setExpirationDuration(final int expirationDuration) {
        this.expirationDuration = expirationDuration;
    }

    public void setExpirationTimeUnit(final TimeUnit expirationTimeUnit) {
        this.expirationTimeUnit = expirationTimeUnit;
    }

    @Override
    public Set<AuditActionContext> getAuditRecordsSince(final LocalDate localDate) {
        return new HashSet<>(0);
    }
}
