package org.apereo.cas.audit;

import org.apereo.cas.audit.spi.AbstractAuditTrailManager;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.util.DateTimeUtils;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link RedisAuditTrailManager}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@Setter
@RequiredArgsConstructor
@SuppressWarnings("JavaUtilDate")
public class RedisAuditTrailManager extends AbstractAuditTrailManager {
    /**
     * Redis key prefix.
     */
    public static final String CAS_AUDIT_CONTEXT_PREFIX = AuditActionContext.class.getSimpleName() + ':';

    private final CasRedisTemplate redisTemplate;

    private final long scanCount;

    public RedisAuditTrailManager(final CasRedisTemplate redisTemplate,
                                  final boolean asynchronous,
                                  final long scanCount) {
        super(asynchronous);
        this.redisTemplate = Objects.requireNonNull(redisTemplate);
        this.scanCount = scanCount;
    }

    private static String getPatternAuditRedisKey(final String time, final String principal) {
        return CAS_AUDIT_CONTEXT_PREFIX + time + ':' + principal;
    }

    private static String getPatternAuditRedisKey() {
        return CAS_AUDIT_CONTEXT_PREFIX + '*';
    }

    @Override
    public Set<? extends AuditActionContext> getAuditRecords(final Map<WhereClauseFields, Object> whereClause) {
        val localDate = (LocalDate) whereClause.get(WhereClauseFields.DATE);
        val dt = DateTimeUtils.dateOf(localDate);
        LOGGER.debug("Retrieving audit records since [{}]", dt);

        val keys = whereClause.containsKey(WhereClauseFields.PRINCIPAL)
            ? getAuditRedisKeys(whereClause.get(WhereClauseFields.PRINCIPAL).toString())
            : getAuditRedisKeys();

        return keys
            .map(redisKey -> redisTemplate.boundValueOps(redisKey).get())
            .filter(Objects::nonNull)
            .map(AuditActionContext.class::cast)
            .filter(audit -> audit.getWhenActionWasPerformed().compareTo(dt) >= 0)
            .collect(Collectors.toSet());
    }

    @Override
    public void removeAll() {
        getAuditRedisKeys().forEach(redisTemplate::delete);
    }

    @Override
    protected void saveAuditRecord(final AuditActionContext audit) {
        val redisKey = getPatternAuditRedisKey(String.valueOf(audit.getWhenActionWasPerformed().getTime()), audit.getPrincipal());
        this.redisTemplate.boundValueOps(redisKey).set(audit);
    }

    private Stream<String> getAuditRedisKeys() {
        return redisTemplate.scan(getPatternAuditRedisKey(), this.scanCount);
    }

    private Stream<String> getAuditRedisKeys(final String principal) {
        return redisTemplate.scan(getPatternAuditRedisKey("*", principal), this.scanCount);
    }
}
