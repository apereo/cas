package org.apereo.cas.audit;

import module java.base;
import org.apereo.cas.audit.spi.AbstractAuditTrailManager;
import org.apereo.cas.redis.core.CasRedisTemplate;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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

    private static final int FETCH_BATCH_SIZE = 100;

    private static final Pattern GLOB_SPECIAL_CHARACTERS = Pattern.compile("([\\\\*?\\[\\]^])");

    private final CasRedisTemplate redisTemplate;

    private Duration maxAge = Duration.ZERO;

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

    private static long getAuditRecordEpochSecond(final String redisKey) {
        val start = CAS_AUDIT_CONTEXT_PREFIX.length();
        val end = redisKey.indexOf(':', start);
        val epoch = end > start ? redisKey.substring(start, end) : StringUtils.EMPTY;
        return NumberUtils.toLong(epoch, Long.MAX_VALUE);
    }

    private static String escapeGlobPattern(final String value) {
        return GLOB_SPECIAL_CHARACTERS.matcher(value).replaceAll("\\\\$1");
    }

    @Override
    public List<? extends AuditActionContext> getAuditRecords(final Map<WhereClauseFields, Object> whereClause) {
        val localDate = (LocalDateTime) whereClause.get(WhereClauseFields.DATE);
        LOGGER.debug("Retrieving audit records since [{}]", localDate);

        val count = whereClause.containsKey(WhereClauseFields.COUNT)
            ? (long) whereClause.get(WhereClauseFields.COUNT)
            : DEFAULT_MAX_AUDIT_RECORDS_TO_FETCH;
        val cutOffEpochSecond = localDate != null ? localDate.toEpochSecond(ZoneOffset.UTC) : Long.MIN_VALUE;

        try (val keys = whereClause.containsKey(WhereClauseFields.PRINCIPAL)
            ? getAuditRedisKeys(whereClause.get(WhereClauseFields.PRINCIPAL).toString())
            : getAuditRedisKeys()) {
            val principal = whereClause.containsKey(WhereClauseFields.PRINCIPAL)
                ? whereClause.get(WhereClauseFields.PRINCIPAL).toString()
                : null;
            val candidateKeys = keys
                .filter(redisKey -> getAuditRecordEpochSecond(redisKey) >= cutOffEpochSecond)
                .sorted(Comparator.comparingLong(RedisAuditTrailManager::getAuditRecordEpochSecond).reversed())
                .toList();

            val results = new ArrayList<AuditActionContext>();
            for (val batch : candidateKeys.stream().gather(Gatherers.windowFixed(FETCH_BATCH_SIZE)).toList()) {
                val values = (List<?>) Objects.requireNonNull(redisTemplate.opsForValue().multiGet(batch));
                values.stream()
                    .filter(Objects::nonNull)
                    .map(AuditActionContext.class::cast)
                    .filter(audit -> localDate == null || audit.getWhenActionWasPerformed().isAfter(localDate))
                    .filter(audit -> principal == null || principal.equals(audit.getPrincipal()))
                    .limit(count - results.size())
                    .forEach(results::add);
                if (results.size() >= count) {
                    break;
                }
            }
            return results;
        }
    }

    @Override
    public void removeAll() {
        try (val keys = getAuditRedisKeys()) {
            keys.forEach(redisTemplate::delete);
        }
    }

    @Override
    protected void saveAuditRecord(final AuditActionContext audit) {
        val redisKey = getPatternAuditRedisKey(String.valueOf(audit.getWhenActionWasPerformed().toEpochSecond(ZoneOffset.UTC)), audit.getPrincipal());
        val operations = redisTemplate.boundValueOps(redisKey);
        if (maxAge != null && maxAge.isPositive()) {
            operations.set(audit, maxAge);
        } else {
            operations.set(audit);
        }
    }

    private Stream<String> getAuditRedisKeys() {
        return redisTemplate.scan(getPatternAuditRedisKey());
    }

    private Stream<String> getAuditRedisKeys(final String principal) {
        return redisTemplate.scan(getPatternAuditRedisKey("*", escapeGlobPattern(principal)));
    }
}
