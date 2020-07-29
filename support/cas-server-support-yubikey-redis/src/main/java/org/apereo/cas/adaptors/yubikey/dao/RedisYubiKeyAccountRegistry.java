package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.adaptors.yubikey.registry.BaseYubiKeyAccountRegistry;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.apache.commons.io.IOUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
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

    @Override
    public boolean registerAccountFor(final YubiKeyDeviceRegistrationRequest request) {
        val accountValidator = getAccountValidator();
        if (accountValidator.isValid(request.getUsername(), request.getToken())) {
            val yubikeyPublicId = getCipherExecutor().encode(accountValidator.getTokenPublicId(request.getToken()));

            val redisKey = getYubiKeyDeviceRedisKey(request.getUsername());
            var account = this.redisTemplate.boundValueOps(redisKey).get();

            val device = YubiKeyRegisteredDevice.builder()
                .id(System.currentTimeMillis())
                .name(request.getName())
                .publicId(yubikeyPublicId)
                .registrationDate(ZonedDateTime.now(Clock.systemUTC()))
                .build();
            if (account == null) {
                account = YubiKeyAccount.builder()
                    .username(request.getUsername())
                    .devices(CollectionUtils.wrapList(device))
                    .build();
                this.redisTemplate.boundValueOps(redisKey).set(account);
                return true;
            }
            account.getDevices().add(device);
            this.redisTemplate.boundValueOps(redisKey).set(account);
            return true;
        }
        return false;
    }

    @Override
    public Collection<? extends YubiKeyAccount> getAccounts() {
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
            .map(account -> {
                val devices = account.getDevices().stream()
                    .filter(device -> getCipherExecutor().decode(device.getPublicId()) != null)
                    .collect(Collectors.toCollection(ArrayList::new));
                account.setDevices(devices);
                return account;
            })
            .collect(Collectors.toList());
    }

    @Override
    public Optional<? extends YubiKeyAccount> getAccount(final String uid) {
        val redisKey = getYubiKeyDeviceRedisKey(uid);
        val account = this.redisTemplate.boundValueOps(redisKey).get();
        if (account != null) {
            val devices = account.getDevices()
                .stream()
                .map(device -> device.setPublicId(getCipherExecutor().decode(device.getPublicId())))
                .collect(Collectors.toCollection(ArrayList::new));
            account.setDevices(devices);
            return Optional.of(account);
        }
        return Optional.empty();
    }

    @Override
    public void delete(final String uid) {
        val redisKey = getYubiKeyDeviceRedisKey(uid);
        this.redisTemplate.delete(redisKey);
    }

    @Override
    public void deleteAll() {
        val keys = (Set<String>) this.redisTemplate.keys(getPatternYubiKeyDevices());
        if (keys != null) {
            this.redisTemplate.delete(keys);
        }
    }

    private static String getPatternYubiKeyDevices() {
        return CAS_YUBIKEY_PREFIX + '*';
    }

    private static String getYubiKeyDeviceRedisKey(final String id) {
        return CAS_YUBIKEY_PREFIX + id;
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
