package org.apereo.cas.u2f.redis;

import org.apereo.cas.adaptors.u2f.storage.BaseU2FDeviceRepository;
import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRegistration;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This is {@link U2FRedisDeviceRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class U2FRedisDeviceRepository extends BaseU2FDeviceRepository {
    /**
     * Redis key prefix.
     */
    public static final String CAS_U2F_PREFIX = U2FRedisDeviceRepository.class.getSimpleName() + ':';

    private final transient RedisTemplate<String, U2FDeviceRegistration> redisTemplate;

    private final long expirationTime;

    private final TimeUnit expirationTimeUnit;

    public U2FRedisDeviceRepository(final LoadingCache<String, String> requestStorage,
                                    final RedisTemplate<String, U2FDeviceRegistration> redisTemplate,
                                    final long expirationTime,
                                    final TimeUnit expirationTimeUnit,
                                    final CipherExecutor<Serializable, String> cipherExecutor) {
        super(requestStorage, cipherExecutor);
        this.expirationTime = expirationTime;
        this.expirationTimeUnit = expirationTimeUnit;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Collection<? extends U2FDeviceRegistration> getRegisteredDevices() {
        val expirationDate = LocalDate.now(ZoneId.systemDefault())
            .minus(this.expirationTime, DateTimeUtils.toChronoUnit(this.expirationTimeUnit));
        val keys = (Set<String>) this.redisTemplate.keys(getPatternRedisKey());
        if (keys != null) {
            return queryDeviceRegistrations(expirationDate, keys);
        }
        return new ArrayList<>(0);
    }

    @Override
    public Collection<? extends U2FDeviceRegistration> getRegisteredDevices(final String username) {
        val expirationDate = LocalDate.now(ZoneId.systemDefault())
            .minus(this.expirationTime, DateTimeUtils.toChronoUnit(this.expirationTimeUnit));
        val keys = (Set<String>) this.redisTemplate.keys(buildRedisKeyForUser(username));
        if (keys != null) {
            return queryDeviceRegistrations(expirationDate, keys);
        }
        return new ArrayList<>(0);
    }

    @Override
    public U2FDeviceRegistration registerDevice(final U2FDeviceRegistration record) {
        val redisKey = buildRedisKeyForRecord(record);
        this.redisTemplate.boundValueOps(redisKey).set(record);
        return this.redisTemplate.boundValueOps(redisKey).get();
    }

    @Override
    public boolean isDeviceRegisteredFor(final String username) {
        return !getRegisteredDevices(username).isEmpty();
    }

    @Override
    public void clean() {
        val expirationDate = LocalDate.now(ZoneId.systemDefault()).minus(this.expirationTime, DateTimeUtils.toChronoUnit(this.expirationTimeUnit));
        LOGGER.debug("Cleaning up expired U2F device registrations based on expiration date [{}]", expirationDate);
        val expiredKeys = getRedisKeys()
            .stream()
            .map(redisKey -> this.redisTemplate.boundValueOps(redisKey).get())
            .filter(Objects::nonNull)
            .map(U2FDeviceRegistration.class::cast)
            .filter(audit -> audit.getCreatedDate().compareTo(expirationDate) <= 0)
            .map(U2FRedisDeviceRepository::buildRedisKeyForRecord)
            .collect(Collectors.toList());
        this.redisTemplate.delete(expiredKeys);
    }

    @Override
    public void removeAll() {
        this.redisTemplate.delete(getRedisKeys());
    }

    @Override
    public void deleteRegisteredDevice(final U2FDeviceRegistration record) {
        val redisKey = buildRedisKeyForRecord(record);
        this.redisTemplate.delete(redisKey);
    }

    private static String getPatternRedisKey() {
        return CAS_U2F_PREFIX + '*';
    }

    private static String buildRedisKeyForRecord(final U2FDeviceRegistration record) {
        return CAS_U2F_PREFIX + record.getUsername() + ':' + record.getId();
    }

    private static String buildRedisKeyForUser(final String username) {
        return CAS_U2F_PREFIX + username + ":*";
    }

    private Collection<? extends U2FDeviceRegistration> queryDeviceRegistrations(final LocalDate expirationDate,
                                                                                 final Set<String> keys) {
        return keys
            .stream()
            .map(redisKey -> this.redisTemplate.boundValueOps(redisKey).get())
            .filter(Objects::nonNull)
            .map(U2FDeviceRegistration.class::cast)
            .filter(audit -> audit.getCreatedDate().compareTo(expirationDate) >= 0)
            .collect(Collectors.toList());
    }

    private Set<String> getRedisKeys() {
        return this.redisTemplate.keys(getPatternRedisKey());
    }
}
