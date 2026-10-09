package org.apereo.cas.web.support;

import module java.base;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.throttle.AbstractInspektrAuditHandlerInterceptorAdapter;
import org.apereo.cas.throttle.ThrottledSubmissionHandlerConfigurationContext;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.data.redis.connection.zset.Tuple;
import org.springframework.data.redis.core.RedisCallback;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Works in conjunction with a redis database to
 * block attempts to dictionary attack users.
 * Failed submissions are tracked in a sorted set per username and client address,
 * scored by the time of failure and expired by Redis once outside the failure range.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class RedisThrottledSubmissionHandlerInterceptorAdapter extends AbstractInspektrAuditHandlerInterceptorAdapter {
    /**
     * Redis key prefix for throttled submissions.
     */
    public static final String THROTTLED_SUBMISSION_PREFIX = "CasThrottledSubmission:";

    private static final long MAX_TRACKED_FAILURES = 2;

    private static final String REQUEST_ATTRIBUTE_FAILURE_RECORDED = RedisThrottledSubmissionHandlerInterceptorAdapter.class.getName() + ".failureRecorded";

    private final CasRedisTemplate<String, Object> redisTemplate;

    public RedisThrottledSubmissionHandlerInterceptorAdapter(
        final ThrottledSubmissionHandlerConfigurationContext configurationContext,
        final CasRedisTemplate<String, Object> redisTemplate) {
        super(configurationContext);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void recordSubmissionFailure(final HttpServletRequest request) {
        if (request.getAttribute(REQUEST_ATTRIBUTE_FAILURE_RECORDED) != null) {
            return;
        }
        request.setAttribute(REQUEST_ATTRIBUTE_FAILURE_RECORDED, Boolean.TRUE);
        val key = buildThrottledSubmissionKey(request);
        val keyBytes = key.getBytes(StandardCharsets.UTF_8);
        val now = (double) Instant.now(Clock.systemUTC()).toEpochMilli();
        val range = getFailureRange();
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            connection.zSetCommands().zAdd(keyBytes, now, UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
            connection.zSetCommands().zRemRange(keyBytes, 0, -(MAX_TRACKED_FAILURES + 1));
            connection.keyCommands().pExpire(keyBytes, range.toMillis());
            return null;
        });
        LOGGER.debug("Recorded submission failure for [{}]", key);
    }

    @Override
    public boolean exceedsThreshold(final HttpServletRequest request) {
        val key = buildThrottledSubmissionKey(request);
        val keyBytes = key.getBytes(StandardCharsets.UTF_8);
        val cutOff = (double) Instant.now(Clock.systemUTC()).minus(getFailureRange()).toEpochMilli();
        val entries = Objects.requireNonNullElseGet(redisTemplate.execute((RedisCallback<Set<Tuple>>) connection ->
            connection.zSetCommands().zRevRangeByScoreWithScores(keyBytes, cutOff, Double.POSITIVE_INFINITY, 0, MAX_TRACKED_FAILURES)), Set::<Tuple>of);
        val failures = entries
            .stream()
            .map(entry -> ThrottledSubmission.builder()
                .key(key)
                .value(Instant.ofEpochMilli(entry.getScore().longValue()).atZone(ZoneOffset.UTC))
                .build())
            .toList();
        return calculateFailureThresholdRateAndCompare(failures);
    }

    @Override
    public void clear() {
        try (val keys = redisTemplate.scan(THROTTLED_SUBMISSION_PREFIX + '*')) {
            keys.forEach(redisTemplate::delete);
        }
    }

    @Override
    public String getName() {
        return "RedisThrottle";
    }

    private String buildThrottledSubmissionKey(final HttpServletRequest request) {
        val username = StringUtils.defaultString(getUsernameParameterFromRequest(request)).toLowerCase(Locale.ROOT);
        val remoteAddress = StringUtils.defaultString(ClientInfoHolder.getClientInfo().getClientIpAddress()).toLowerCase(Locale.ROOT);
        return THROTTLED_SUBMISSION_PREFIX + remoteAddress + ':' + username;
    }

    private Duration getFailureRange() {
        val throttle = getConfigurationContext().getCasProperties().getAuthn().getThrottle().getFailure();
        return Duration.ofSeconds(Math.max(1, throttle.getRangeSeconds()));
    }
}
