package org.apereo.cas.audit;

import module java.base;
import org.apereo.cas.audit.spi.AbstractAuditTrailManager;
import org.apereo.cas.redis.core.CasRedisTemplate;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;

/**
 * This is {@link RedisAuditTrailManager}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@Setter
@RequiredArgsConstructor
public class RedisAuditTrailManager extends AbstractAuditTrailManager {
    /**
     * Redis key prefix.
     */
    public static final String CAS_AUDIT_CONTEXT_PREFIX = AuditActionContext.class.getSimpleName() + ':';

    private final CasRedisTemplate redisTemplate;


    public RedisAuditTrailManager(final CasRedisTemplate redisTemplate,
                                  final boolean asynchronous) {
        super(asynchronous);
        this.redisTemplate = Objects.requireNonNull(redisTemplate);
    }

    private static String getPatternAuditRedisKey(final String time, final String principal) {
        return CAS_AUDIT_CONTEXT_PREFIX + time + ':' + principal;
    }

    private static String getPatternAuditRedisKey() {
        return CAS_AUDIT_CONTEXT_PREFIX + '*';
    }

    @Override
    public List<? extends AuditActionContext> getAuditRecords(final Map<WhereClauseFields, Object> whereClause) {
        val localDate = (LocalDateTime) whereClause.get(WhereClauseFields.DATE);
        LOGGER.debug("Retrieving audit records since [{}]", localDate);

        val count = whereClause.containsKey(WhereClauseFields.COUNT)
            ? (long) whereClause.get(WhereClauseFields.COUNT)
            : DEFAULT_MAX_AUDIT_RECORDS_TO_FETCH;
        
        try (val keys = whereClause.containsKey(WhereClauseFields.PRINCIPAL)
            ? getAuditRedisKeys(whereClause.get(WhereClauseFields.PRINCIPAL).toString(), count)
            : getAuditRedisKeys(count)) {
            return keys
                .limit(count)
                .map(redisKey -> redisTemplate.boundValueOps(redisKey).get())
                .filter(Objects::nonNull)
                .map(AuditActionContext.class::cast)
                .filter(audit -> audit.getWhenActionWasPerformed().isAfter(localDate))
                .collect(Collectors.toList());
        }
    }

    @Override
    public void removeAll() {
        try (val keys = getAuditRedisKeys(-1)) {
            keys.forEach(redisTemplate::delete);
        }
    }

    @Override
    protected void saveAuditRecord(final AuditActionContext audit) {
        val redisKey = getPatternAuditRedisKey(String.valueOf(audit.getWhenActionWasPerformed().toEpochSecond(ZoneOffset.UTC)), audit.getPrincipal());
        this.redisTemplate.boundValueOps(redisKey).set(audit);
    }

    private Stream<String> getAuditRedisKeys(final long count) {
        return redisTemplate.scan(getPatternAuditRedisKey(), count);
    }

    private Stream<String> getAuditRedisKeys(final String principal, final long count) {
        return redisTemplate.scan(getPatternAuditRedisKey("*", principal), count);
    }
}
