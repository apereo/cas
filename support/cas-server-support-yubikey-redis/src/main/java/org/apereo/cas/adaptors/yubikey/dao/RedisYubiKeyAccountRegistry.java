package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.registry.BaseYubiKeyAccountRegistry;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.io.IOException;
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
@Slf4j
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
    public boolean registerAccountFor(final String uid, final String token) {
        val accountValidator = getAccountValidator();
        if (accountValidator.isValid(uid, token)) {
            val yubikeyPublicId = getCipherExecutor().encode(accountValidator.getTokenPublicId(token));
            val redisKey = getYubiKeyDeviceRedisKey(uid);
            var account = this.redisTemplate.boundValueOps(redisKey).get();
            if (account == null) {
                account = new YubiKeyAccount();
                account.setUsername(uid);
                account.registerDevice(yubikeyPublicId);
                this.redisTemplate.boundValueOps(redisKey).set(account);
                return true;
            }
            account.registerDevice(yubikeyPublicId);
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
                val devices = account.getDeviceIdentifiers().stream()
                    .map(pubId -> getCipherExecutor().decode(pubId))
                    .collect(Collectors.toCollection(ArrayList::new));
                return new YubiKeyAccount(account.getId(), devices, account.getUsername());
            })
            .collect(Collectors.toList());

    }

    @Override
    public Optional<? extends YubiKeyAccount> getAccount(final String uid) {
        val redisKey = getYubiKeyDeviceRedisKey(uid);
        val account = this.redisTemplate.boundValueOps(redisKey).get();

        if (account != null) {
            val devices = account.getDeviceIdentifiers().stream()
                .map(pubId -> getCipherExecutor().decode(pubId))
                .collect(Collectors.toCollection(ArrayList::new));
            val yubiAccount = new YubiKeyAccount(account.getId(), devices, account.getUsername());
            return Optional.of(yubiAccount);
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

    private Stream<String> getYubiKeyDevicesStream() {
        val cursor = Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection()
            .scan(ScanOptions.scanOptions().match(getPatternYubiKeyDevices()).build());
        return StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(cursor, Spliterator.ORDERED), false)
            .map(key -> (String) redisTemplate.getKeySerializer().deserialize(key))
            .collect(Collectors.toSet())
            .stream()
            .onClose(() -> {
                try {
                    cursor.close();
                } catch (final IOException e) {
                    LOGGER.error("Could not close Redis connection", e);
                }
            });
    }
}
