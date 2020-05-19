package org.apereo.cas.u2f.redis;

import org.apereo.cas.adaptors.u2f.storage.BaseU2FDeviceRepository;
import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRegistration;
import org.apereo.cas.util.DateTimeUtils;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.yubico.u2f.data.DeviceRegistration;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;

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

    private final transient RedisTemplate redisTemplate;

    private final long expirationTime;

    private final TimeUnit expirationTimeUnit;

    public U2FRedisDeviceRepository(final LoadingCache<String, String> requestStorage,
                                    final RedisTemplate redisTemplate,
                                    final long expirationTime,
                                    final TimeUnit expirationTimeUnit) {
        super(requestStorage);
        this.expirationTime = expirationTime;
        this.expirationTimeUnit = expirationTimeUnit;
        this.redisTemplate = redisTemplate;
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

    @Override
    public Collection<? extends DeviceRegistration> getRegisteredDevices(final String username) {
        try {
            val expirationDate = LocalDate.now(ZoneId.systemDefault())
                .minus(this.expirationTime, DateTimeUtils.toChronoUnit(this.expirationTimeUnit));
            val keys = (Set<String>) this.redisTemplate.keys(buildRedisKeyForUser(username));
            if (keys != null) {
                return keys
                    .stream()
                    .map(redisKey -> this.redisTemplate.boundValueOps(redisKey).get())
                    .filter(Objects::nonNull)
                    .map(U2FDeviceRegistration.class::cast)
                    .filter(audit -> audit.getCreatedDate().compareTo(expirationDate) >= 0)
                    .map(r -> {
                        try {
                            val decoded = getCipherExecutor().decode(r.getRecord());
                            if (StringUtils.isNotBlank(decoded)) {
                                return DeviceRegistration.fromJson(decoded);
                            }
                            LOGGER.warn("Unable to to decode device registration for record id [{}]", r.getId());
                        } catch (final Exception e) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.error(e.getMessage(), e);
                            } else {
                                LOGGER.error(e.getMessage());
                            }
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            }
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return new ArrayList<>(0);
    }

    @Override
    public void registerDevice(final String username, final DeviceRegistration registration) {
        val record = new U2FDeviceRegistration();
        record.setUsername(username);
        record.setRecord(getCipherExecutor().encode(registration.toJsonWithAttestationCert()));
        record.setCreatedDate(LocalDate.now(ZoneId.systemDefault()));
        val redisKey = buildRedisKeyForRecord(record);
        this.redisTemplate.boundValueOps(redisKey).set(record);
    }

    @Override
    public boolean isDeviceRegisteredFor(final String username) {
        return !getRegisteredDevices(username).isEmpty();
    }

    @Override
    public void clean() {
        try {
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
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
    }

    @Override
    public void removeAll() {
        this.redisTemplate.delete(getRedisKeys());
    }

    private Set<String> getRedisKeys() {
        return this.redisTemplate.keys(getPatternRedisKey());
    }
}
