package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.gauth.CasGoogleAuthenticator;
import org.apereo.cas.gauth.RedisCompositeKey;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * This is {@link RedisGoogleAuthenticatorTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@ToString
@Getter
public class RedisGoogleAuthenticatorTokenCredentialRepository extends BaseGoogleAuthenticatorTokenCredentialRepository {

    private final CasRedisTemplates casRedisTemplates;

    public RedisGoogleAuthenticatorTokenCredentialRepository(
        final CasGoogleAuthenticator googleAuthenticator,
        final CasRedisTemplates casRedisTemplates,
        final CipherExecutor<String, String> tokenCredentialCipher,
        final CipherExecutor<Number, Number> scratchCodesCipher) {
        super(tokenCredentialCipher, scratchCodesCipher, googleAuthenticator);
        this.casRedisTemplates = casRedisTemplates;
    }

    @Override
    public OneTimeTokenAccount get(final String username, final long id) {
        return get(username)
            .stream()
            .filter(account -> account.getId() == id)
            .findFirst()
            .orElse(null);
    }

    @Override
    public OneTimeTokenAccount get(final long id) {
        val redisAccountKey = RedisCompositeKey.forAccounts().withAccount(id).toKeyPattern();
        val account = casRedisTemplates.getAccountsRedisTemplate().boundValueOps(redisAccountKey).get();
        return account != null ? decode(account) : null;
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> get(final String username) {
        val redisAccountKey = RedisCompositeKey.forPrincipals().withPrincipal(username).toKeyPattern();
        val accounts = casRedisTemplates.getPrincipalsRedisTemplate().boundSetOps(redisAccountKey).members();
        return Objects.requireNonNull(accounts)
            .stream()
            .filter(Objects::nonNull)
            .map(this::decode)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> load() {
        val keyPattern = RedisCompositeKey.forAccounts().toKeyPattern();
        val accounts = casRedisTemplates.getAccountsRedisTemplate().keys(keyPattern);
        return Objects.requireNonNull(accounts)
            .stream()
            .map(redisKey -> casRedisTemplates.getAccountsRedisTemplate().boundValueOps(redisKey).get())
            .filter(Objects::nonNull)
            .map(this::decode)
            .collect(Collectors.toList());
    }

    @Override
    public OneTimeTokenAccount save(final OneTimeTokenAccount account) {
        return update(account.assignIdIfNecessary());
    }

    @Override
    public OneTimeTokenAccount update(final OneTimeTokenAccount account) {
        val encodedAccount = encode(account);

        val redisAccountKey = RedisCompositeKey.forAccounts().withAccount(encodedAccount).toKeyPattern();
        LOGGER.trace("Saving account [{}] using key [{}]", encodedAccount, redisAccountKey);
        casRedisTemplates.getAccountsRedisTemplate().boundValueOps(redisAccountKey).set(account);

        val redisPrincipalKey = RedisCompositeKey.forPrincipals().withPrincipal(encodedAccount).toKeyPattern();
        LOGGER.trace("Saving principal [{}] using key [{}]", encodedAccount, redisPrincipalKey);
        casRedisTemplates.getPrincipalsRedisTemplate().boundSetOps(redisPrincipalKey).add(encodedAccount);

        return encodedAccount;
    }

    @Override
    public void deleteAll() {
        var options = ScanOptions.scanOptions().match(RedisCompositeKey.forAccounts().toKeyPattern()).build();
        try (val result = casRedisTemplates.getAccountsRedisTemplate().scan(options)) {
            casRedisTemplates.getAccountsRedisTemplate().executePipelined((RedisCallback<Object>) connection -> {
                StreamSupport.stream(result.spliterator(), false)
                    .forEach(id -> connection.keyCommands().del(id.getBytes(StandardCharsets.UTF_8)));
                return null;
            });
        }
        options = ScanOptions.scanOptions().match(RedisCompositeKey.forPrincipals().toKeyPattern()).build();
        try (val result = casRedisTemplates.getPrincipalsRedisTemplate().scan(options)) {
            casRedisTemplates.getPrincipalsRedisTemplate().executePipelined((RedisCallback<Object>) connection -> {
                StreamSupport.stream(result.spliterator(), false)
                    .forEach(id -> connection.keyCommands().del(id.getBytes(StandardCharsets.UTF_8)));
                return null;
            });
        }
    }

    @Override
    public void delete(final String username) {
        val redisKeyPattern = RedisCompositeKey.forPrincipals().withPrincipal(username).toKeyPattern();
        val accounts = casRedisTemplates.getPrincipalsRedisTemplate().boundSetOps(redisKeyPattern).members();
        casRedisTemplates.getAccountsRedisTemplate().executePipelined((RedisCallback<Object>) connection -> {
            Objects.requireNonNull(accounts).forEach(account -> {
                val accountKey = RedisCompositeKey.forAccounts().withAccount(account).toKeyPattern();
                connection.keyCommands().del(accountKey.getBytes(StandardCharsets.UTF_8));
            });
            return null;
        });
        casRedisTemplates.getPrincipalsRedisTemplate().delete(redisKeyPattern);
    }

    @Override
    public void delete(final long id) {
        val accountKey = RedisCompositeKey.forAccounts().withAccount(id).toKeyPattern();
        casRedisTemplates.getAccountsRedisTemplate().delete(accountKey);
    }

    @Override
    public long count() {
        val redisKeyPattern = RedisCompositeKey.forAccounts().toKeyPattern();
        val accounts = Objects.requireNonNull(casRedisTemplates.getAccountsRedisTemplate().keys(redisKeyPattern));
        return accounts.isEmpty() ? 0 : casRedisTemplates.getAccountsRedisTemplate().countExistingKeys(accounts);
    }

    @Override
    public long count(final String username) {
        val redisKeyPattern = RedisCompositeKey.forPrincipals().withPrincipal(username).toKeyPattern();
        return casRedisTemplates.getPrincipalsRedisTemplate().boundSetOps(redisKeyPattern).size();
    }

    @Data
    public static class CasRedisTemplates {
        private final CasRedisTemplate<String, OneTimeTokenAccount> accountsRedisTemplate;

        private final CasRedisTemplate<String, OneTimeTokenAccount> principalsRedisTemplate;
    }
}
