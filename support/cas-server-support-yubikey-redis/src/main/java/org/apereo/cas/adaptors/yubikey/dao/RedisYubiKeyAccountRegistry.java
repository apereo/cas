package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.adaptors.yubikey.registry.BaseYubiKeyAccountRegistry;

import lombok.val;
import org.apache.commons.io.IOUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    private final RedisTemplate<String, YubiKeyAccount> redisTemplate;

    public RedisYubiKeyAccountRegistry(final YubiKeyAccountValidator accountValidator,
                                       final RedisTemplate<String, YubiKeyAccount> mongoTemplate) {
        super(accountValidator);
        this.redisTemplate = mongoTemplate;
    }

    private static String getPatternYubiKeyDevices() {
        return CAS_YUBIKEY_PREFIX + '*';
    }

    private static String getYubiKeyDeviceRedisKey(final String id) {
        return CAS_YUBIKEY_PREFIX + id;
    }

    @Override
    public Collection<? extends YubiKeyAccount> getAccountsInternal() {
        return getYubiKeyDevicesStream()
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
        val keys = (Set<String>) this.redisTemplate.keys(getPatternYubiKeyDevices());
        if (keys != null) {
            this.redisTemplate.delete(keys);
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

    private Stream<String> getYubiKeyDevicesStream() {
        val cursor = Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection()
            .scan(ScanOptions.scanOptions().match(getPatternYubiKeyDevices()).build());
        return StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(cursor, Spliterator.ORDERED), false)
            .map(key -> (String) redisTemplate.getKeySerializer().deserialize(key))
            .collect(Collectors.toSet())
            .stream()
            .onClose(() -> IOUtils.closeQuietly(cursor));
    }
}
