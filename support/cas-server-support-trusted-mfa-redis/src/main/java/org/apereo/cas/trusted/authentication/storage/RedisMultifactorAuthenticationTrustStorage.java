package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.configuration.model.support.mfa.trusteddevice.TrustedDevicesMultifactorProperties;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.redis.core.RedisCallback;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    private final CasRedisTemplate<String, List<MultifactorAuthenticationTrustRecord>> redisTemplate;

    public RedisMultifactorAuthenticationTrustStorage(
        final TrustedDevicesMultifactorProperties properties,
        final CipherExecutor<Serializable, String> cipherExecutor,
        final CasRedisTemplate<String, List<MultifactorAuthenticationTrustRecord>> redisTemplate,
        final MultifactorAuthenticationTrustRecordKeyGenerator keyGenerationStrategy) {
        super(properties, cipherExecutor, keyGenerationStrategy);
        this.redisTemplate = redisTemplate;
    }

    private static String getPatternRedisKey() {
        return CAS_PREFIX + '*';
    }

    private static String buildRedisKeyForRecord(final String username) {
        return CAS_PREFIX + username.toLowerCase(Locale.ENGLISH) + ":*";
    }

    private static String buildRedisKeyForRecord(final long id) {
        return CAS_PREFIX + "*:" + id;
    }

    private static String buildRedisKeyForRecord(final MultifactorAuthenticationTrustRecord record) {
        return CAS_PREFIX + record.getPrincipal().toLowerCase(Locale.ENGLISH) + ':' + record.getId();
    }

    @Override
    public void remove(final String key) {
        val principal = getKeyGenerationStrategy().getPrincipalFromRecordKey(getCipherExecutor().decode(key));
        try (val results = redisTemplate.scan(buildRedisKeyForRecord(principal))) {
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                StreamSupport.stream(results.spliterator(), false).forEach(id ->
                    connection.keyCommands().del(id.getBytes(StandardCharsets.UTF_8)));
                return null;
            });
        }
    }

    @Override
    public void remove(final ZonedDateTime expirationDate) {
        try (val results = redisTemplate.scan(getPatternRedisKey())) {
            results
                .map(redisKey -> redisTemplate.boundValueOps(redisKey).get())
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(record -> DateTimeUtils.zonedDateTimeOf(record.getExpirationDate()).isBefore(expirationDate))
                .forEach(record -> {
                    try (val recordKeys = redisTemplate.scan(buildRedisKeyForRecord(record))) {
                        redisTemplate.delete(Objects.requireNonNull(recordKeys.collect(Collectors.toSet())));
                    }
                });
        }
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> getAll() {
        try (val keys = redisTemplate.scan(getPatternRedisKey())) {
            return getFromRedisKeys(keys);
        }
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final ZonedDateTime onOrAfterDate) {
        return getAll()
            .stream()
            .filter(record -> record.getRecordDate().isAfter(onOrAfterDate))
            .collect(Collectors.toSet());
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final String principal) {
        try (val keys = redisTemplate.scan(buildRedisKeyForRecord(principal))) {
            return getFromRedisKeys(keys);
        }
    }

    @Override
    public MultifactorAuthenticationTrustRecord get(final long id) {
        try (val keys = redisTemplate.scan(buildRedisKeyForRecord(id))) {
            return keys
                .findFirst()
                .map(redisKey -> {
                    val results = this.redisTemplate.boundValueOps(redisKey).get();
                    if (results != null && !results.isEmpty()) {
                        return results.getFirst();
                    }
                    return null;
                })
                .orElse(null);
        }
    }

    @Override
    protected MultifactorAuthenticationTrustRecord saveInternal(final MultifactorAuthenticationTrustRecord record) {
        val redisKey = buildRedisKeyForRecord(record);
        val valueOps = redisTemplate.boundValueOps(redisKey);
        val results = ObjectUtils.getIfNull(valueOps.get(), new ArrayList<MultifactorAuthenticationTrustRecord>());
        results.add(record);
        valueOps.set(results);
        valueOps.expireAt(record.getExpirationDate().toInstant());
        return record;
    }

    private Set<? extends MultifactorAuthenticationTrustRecord> getFromRedisKeys(final Stream<String> keys) {
        val expirationDate = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        return keys
            .map(redisKey -> redisTemplate.boundValueOps(redisKey).get())
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .filter(record -> DateTimeUtils.zonedDateTimeOf(record.getExpirationDate()).isAfter(expirationDate))
            .collect(Collectors.toSet());
    }
}
