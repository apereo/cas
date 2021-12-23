package org.apereo.cas.u2f.redis;

import org.apereo.cas.adaptors.u2f.storage.BaseU2FDeviceRepository;
import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRegistration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.util.RedisUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private final RedisTemplate<String, U2FDeviceRegistration> redisTemplate;

    public U2FRedisDeviceRepository(final LoadingCache<String, String> requestStorage,
                                    final RedisTemplate<String, U2FDeviceRegistration> redisTemplate,
                                    final CipherExecutor<Serializable, String> cipherExecutor,
                                    final CasConfigurationProperties casProperties) {
        super(casProperties, requestStorage, cipherExecutor);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Collection<? extends U2FDeviceRegistration> getRegisteredDevices() {
        val expirationDate = getDeviceExpiration();
        val keys = RedisUtils.keys(this.redisTemplate, getPatternRedisKey(),
            casProperties.getAuthn().getMfa().getU2f().getRedis().getScanCount());
        return queryDeviceRegistrations(expirationDate, keys);
    }

    @Override
    public Collection<? extends U2FDeviceRegistration> getRegisteredDevices(final String username) {
        val expirationDate = getDeviceExpiration();
        val keys = RedisUtils.keys(this.redisTemplate, buildRedisKeyForUser(username),
            casProperties.getAuthn().getMfa().getU2f().getRedis().getScanCount());
        return queryDeviceRegistrations(expirationDate, keys);
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
        val expirationDate = getDeviceExpiration();
        LOGGER.debug("Cleaning up expired U2F device registrations based on expiration date [{}]", expirationDate);
        val expiredKeys = getRedisKeys()
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
        this.redisTemplate.delete(getRedisKeys().collect(Collectors.toSet()));
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
                                                                                 final Stream<String> keys) {
        return keys
            .map(redisKey -> this.redisTemplate.boundValueOps(redisKey).get())
            .filter(Objects::nonNull)
            .map(U2FDeviceRegistration.class::cast)
            .filter(audit -> audit.getCreatedDate().compareTo(expirationDate) >= 0)
            .collect(Collectors.toList());
    }

    private Stream<String> getRedisKeys() {
        return RedisUtils.keys(this.redisTemplate, getPatternRedisKey(),
            casProperties.getAuthn().getMfa().getU2f().getRedis().getScanCount());
    }
}
