package org.apereo.cas.webauthn;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.webauthn.storage.BaseWebAuthnCredentialRepository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yubico.data.CredentialRegistration;
import lombok.SneakyThrows;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
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

    private final RedisTemplate<String, RedisWebAuthnCredentialRegistration> redisTemplate;

    public RedisWebAuthnCredentialRepository(
        final RedisTemplate<String, RedisWebAuthnCredentialRegistration> redisTemplate,
        final CasConfigurationProperties properties,
        final CipherExecutor<String, String> cipherExecutor) {
        super(properties, cipherExecutor);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Collection<CredentialRegistration> getRegistrationsByUsername(final String username) {
        val keys = (Set<String>) this.redisTemplate.keys(buildRedisKeyForRecord(username));
        if (keys != null) {
            return toCredentialRegistrationsAsStream(keys).collect(Collectors.toSet());
        }
        return new ArrayList<>(0);
    }

    @Override
    public Stream<CredentialRegistration> stream() {
        val keys = (Set<String>) this.redisTemplate.keys(getPatternRedisKey());
        if (keys != null) {
            return toCredentialRegistrationsAsStream(keys);
        }
        return Stream.empty();
    }

    @Override
    @SneakyThrows
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
            val jsonRecords = getCipherExecutor().encode(WebAuthnUtils.getObjectMapper().writeValueAsString(records));
            val entry = RedisWebAuthnCredentialRegistration.builder()
                .records(jsonRecords)
                .username(username.trim().toLowerCase())
                .build();
            redisTemplate.boundValueOps(redisKey).set(entry);
        }
    }

    private Stream<CredentialRegistration> toCredentialRegistrationsAsStream(final Set<String> keys) {
        return keys
            .stream()
            .map(redisKey -> this.redisTemplate.boundValueOps(redisKey).get())
            .filter(Objects::nonNull)
            .map(record -> getCipherExecutor().decode(record.getRecords()))
            .map(Unchecked.function(record -> WebAuthnUtils.getObjectMapper().readValue(record, new TypeReference<Set<CredentialRegistration>>() {
            })))
            .flatMap(Collection::stream);
    }

    private static String getPatternRedisKey() {
        return CAS_WEB_AUTHN_PREFIX + '*';
    }

    private static String buildRedisKeyForRecord(final String username) {
        return CAS_WEB_AUTHN_PREFIX + username.trim().toLowerCase();
    }
}
