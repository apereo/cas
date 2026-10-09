package org.apereo.cas.adaptors.yubikey.dao;

import module java.base;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.adaptors.yubikey.registry.BaseYubiKeyAccountRegistry;
import org.apereo.cas.redis.core.CasRedisTemplate;
import lombok.val;

/**
 * This is {@link RedisYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class RedisYubiKeyAccountRegistry extends BaseYubiKeyAccountRegistry {
    /**
     * Redis key prefix.
     */
    public static final String CAS_YUBIKEY_PREFIX = RedisYubiKeyAccountRegistry.class.getSimpleName() + ':';

    private final CasRedisTemplate<String, YubiKeyAccount> redisTemplate;

    public RedisYubiKeyAccountRegistry(final YubiKeyAccountValidator accountValidator,
                                       final CasRedisTemplate<String, YubiKeyAccount> mongoTemplate) {
        super(accountValidator);
        this.redisTemplate = mongoTemplate;
    }

    private static String getPatternYubiKeyDevices() {
        return CAS_YUBIKEY_PREFIX + '*';
    }

    private static String getYubiKeyDeviceRedisKey(final String id) {
        return CAS_YUBIKEY_PREFIX + id;
    }

    /**
     * Load every account. The key scan is closed when done, which closes the cursor and returns its connection;
     * with a connection pool, a connection that is never returned is lost to the pool.
     *
     * @return the accounts
     */
    @Override
    public Collection<? extends YubiKeyAccount> getAccountsInternal() {
        try (val keys = redisTemplate.scan(getPatternYubiKeyDevices())) {
            return keys
                .map(redisKey -> {
                    val device = redisTemplate.boundValueOps(redisKey).get();
                    if (device == null) {
                        this.redisTemplate.delete(redisKey);
                        return null;
                    }
                    return device;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        }
    }

    @Override
    public YubiKeyAccount getAccountInternal(final String uid) {
        val redisKey = getYubiKeyDeviceRedisKey(uid);
        return this.redisTemplate.boundValueOps(redisKey).get();
    }

    @Override
    public void delete(final String uid) {
        val redisKey = getYubiKeyDeviceRedisKey(uid);
        this.redisTemplate.delete(redisKey);
    }

    @Override
    public void delete(final String username, final long deviceId) {
        val redisKey = getYubiKeyDeviceRedisKey(username);
        val account = this.redisTemplate.boundValueOps(redisKey).get();
        if (account != null && account.getDevices().removeIf(device -> device.getId() == deviceId)) {
            this.redisTemplate.boundValueOps(redisKey).set(account);
        }
    }

    @Override
    public void deleteAll() {
        try (val keys = redisTemplate.scan(getPatternYubiKeyDevices())) {
            this.redisTemplate.delete(keys.collect(Collectors.toSet()));
        }
    }

    @Override
    public YubiKeyAccount save(final YubiKeyDeviceRegistrationRequest request,
                               final YubiKeyRegisteredDevice... device) {
        val account = YubiKeyAccount.builder()
            .username(request.getUsername())
            .devices(Arrays.stream(device).collect(Collectors.toList()))
            .build();
        return save(account);
    }

    @Override
    public YubiKeyAccount save(final YubiKeyAccount account) {
        val redisKey = getYubiKeyDeviceRedisKey(account.getUsername());
        this.redisTemplate.boundValueOps(redisKey).set(account);
        return account;
    }

    @Override
    public boolean update(final YubiKeyAccount account) {
        val redisKey = getYubiKeyDeviceRedisKey(account.getUsername());
        this.redisTemplate.boundValueOps(redisKey).set(account);
        return true;
    }
}
