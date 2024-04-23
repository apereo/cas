package org.apereo.cas.webauthn;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.webauthn.storage.BaseWebAuthnCredentialRepository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yubico.data.CredentialRegistration;
import lombok.val;
import org.jooq.lambda.Unchecked;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link RedisWebAuthnCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class RedisWebAuthnCredentialRepository extends BaseWebAuthnCredentialRepository {
    /**
     * Redis key prefix.
     */
    public static final String CAS_WEB_AUTHN_PREFIX = RedisWebAuthnCredentialRepository.class.getSimpleName() + ':';

    private final CasRedisTemplate<String, RedisWebAuthnCredentialRegistration> redisTemplate;

    private final long scanCount;

    public RedisWebAuthnCredentialRepository(
        final CasRedisTemplate<String, RedisWebAuthnCredentialRegistration> redisTemplate,
        final CasConfigurationProperties properties,
        final CipherExecutor<String, String> cipherExecutor) {
        super(properties, cipherExecutor);
        this.redisTemplate = redisTemplate;
        this.scanCount = properties.getAuthn().getMfa().getWebAuthn().getRedis().getScanCount();
    }

    @Override
    public Collection<CredentialRegistration> getRegistrationsByUsername(final String username) {
        try (val keys = redisTemplate.scan(buildRedisKeyForRecord(username), this.scanCount)) {
            return toCredentialRegistrationsAsStream(keys).collect(Collectors.toSet());
        }
    }

    @Override
    public Stream<CredentialRegistration> stream() {
        try (val keys = redisTemplate.scan(getPatternRedisKey(), this.scanCount)) {
            return toCredentialRegistrationsAsStream(keys);
        }
    }

    @Override
    protected void update(final String username, final Collection<CredentialRegistration> givenRecords) {
        val redisKey = buildRedisKeyForRecord(username);
        if (givenRecords.isEmpty()) {
            redisTemplate.delete(redisKey);
        } else {
            val records = givenRecords.stream()
                .map(record -> {
                    if (record.getRegistrationTime() == null) {
                        return record.withRegistrationTime(Instant.now(Clock.systemUTC()));
                    }
                    return record;
                })
                .collect(Collectors.toList());
            val jsonRecords = FunctionUtils.doUnchecked(() -> getCipherExecutor().encode(WebAuthnUtils.getObjectMapper().writeValueAsString(records)));
            val entry = RedisWebAuthnCredentialRegistration.builder()
                .records(jsonRecords)
                .username(username.trim().toLowerCase(Locale.ENGLISH))
                .build();
            redisTemplate.boundValueOps(redisKey).set(entry);
        }
    }

    private Stream<CredentialRegistration> toCredentialRegistrationsAsStream(final Stream<String> keys) {
        return keys
            .map(redisKey -> this.redisTemplate.boundValueOps(redisKey).get())
            .filter(Objects::nonNull)
            .map(record -> getCipherExecutor().decode(record.getRecords()))
            .filter(Objects::nonNull)
            .map(Unchecked.function(record -> WebAuthnUtils.getObjectMapper().readValue(record, new TypeReference<Set<CredentialRegistration>>() {
            })))
            .flatMap(Collection::stream)
            .toList()
            .stream();
    }

    private static String getPatternRedisKey() {
        return CAS_WEB_AUTHN_PREFIX + '*';
    }

    private static String buildRedisKeyForRecord(final String username) {
        return CAS_WEB_AUTHN_PREFIX + username.trim().toLowerCase(Locale.ENGLISH);
    }
}
