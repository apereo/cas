package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.configuration.model.support.mfa.trusteddevice.TrustedDevicesMultifactorProperties;
import org.apereo.cas.redis.core.util.RedisUtils;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link RedisMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class RedisMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {
    /**
     * Redis key prefix.
     */
    public static final String CAS_PREFIX = RedisMultifactorAuthenticationTrustStorage.class.getSimpleName() + ':';

    private final RedisTemplate<String, List<MultifactorAuthenticationTrustRecord>> redisTemplate;

    private final long scanCount;

    public RedisMultifactorAuthenticationTrustStorage(
        final TrustedDevicesMultifactorProperties properties,
        final CipherExecutor<Serializable, String> cipherExecutor,
        final RedisTemplate<String, List<MultifactorAuthenticationTrustRecord>> redisTemplate,
        final MultifactorAuthenticationTrustRecordKeyGenerator keyGenerationStrategy,
        final long scanCount) {
        super(properties, cipherExecutor, keyGenerationStrategy);
        this.redisTemplate = redisTemplate;
        this.scanCount = scanCount;
    }

    private static String getPatternRedisKey() {
        return CAS_PREFIX + '*';
    }

    private static String buildRedisKeyForRecord(final String username) {
        return CAS_PREFIX + username + ":*";
    }

    private static String buildRedisKeyForRecord(final long id) {
        return CAS_PREFIX + "*:" + id;
    }

    private static String buildRedisKeyForRecord(final MultifactorAuthenticationTrustRecord record) {
        return CAS_PREFIX + record.getPrincipal() + ':' + record.getId();
    }

    @Override
    public void remove(final String key) {
        val principal = getKeyGenerationStrategy().getPrincipalFromRecordKey(getCipherExecutor().decode(key));
        RedisUtils.keys(redisTemplate, buildRedisKeyForRecord(principal), this.scanCount)
            .findFirst()
            .ifPresent(redisTemplate::delete);
    }

    @Override
    public void remove(final ZonedDateTime expirationDate) {
        RedisUtils.keys(redisTemplate, getPatternRedisKey(), this.scanCount)
            .map(redisKey -> redisTemplate.boundValueOps(redisKey).get())
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .filter(record -> DateTimeUtils.zonedDateTimeOf(record.getExpirationDate()).isBefore(expirationDate))
            .forEach(record -> {
                val recordKeys = RedisUtils.keys(redisTemplate, buildRedisKeyForRecord(record), this.scanCount)
                    .collect(Collectors.toSet());
                redisTemplate.delete(Objects.requireNonNull(recordKeys));
            });
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> getAll() {
        remove();
        val keys = RedisUtils.keys(this.redisTemplate, getPatternRedisKey(), this.scanCount);
        return getFromRedisKeys(keys);
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final ZonedDateTime onOrAfterDate) {
        remove();
        return getAll()
            .stream()
            .filter(record -> record.getRecordDate().isAfter(onOrAfterDate))
            .collect(Collectors.toSet());
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final String principal) {
        remove();
        val keys = RedisUtils.keys(this.redisTemplate, buildRedisKeyForRecord(principal), this.scanCount);
        return getFromRedisKeys(keys);
    }

    @Override
    public MultifactorAuthenticationTrustRecord get(final long id) {
        remove();
        return RedisUtils.keys(this.redisTemplate, buildRedisKeyForRecord(id), this.scanCount)
            .findFirst()
            .map(redisKey -> {
                val results = this.redisTemplate.boundValueOps(redisKey).get();
                if (results != null && !results.isEmpty()) {
                    return results.get(0);
                }
                return null;
            })
            .orElse(null);
    }

    @Override
    protected MultifactorAuthenticationTrustRecord saveInternal(final MultifactorAuthenticationTrustRecord record) {
        val redisKey = buildRedisKeyForRecord(record);
        val results = (List<MultifactorAuthenticationTrustRecord>)
            ObjectUtils.defaultIfNull(redisTemplate.boundValueOps(redisKey).get(), new ArrayList<>());
        results.add(record);
        redisTemplate.boundValueOps(redisKey).set(results);
        return record;
    }

    private Set<? extends MultifactorAuthenticationTrustRecord> getFromRedisKeys(final Stream<String> keys) {
        return keys
            .map(redisKey -> redisTemplate.boundValueOps(redisKey).get())
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .collect(Collectors.toSet());
    }
}
