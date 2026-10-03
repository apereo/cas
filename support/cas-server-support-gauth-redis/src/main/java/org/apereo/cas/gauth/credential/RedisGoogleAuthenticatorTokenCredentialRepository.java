package org.apereo.cas.gauth.credential;

import module java.base;
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
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.RedisSerializer;

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
    private static final int PRINCIPAL_KEYS_BATCH_SIZE = 500;

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
    public @Nullable OneTimeTokenAccount get(final String username, final long id) {
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
        val principalOps = casRedisTemplates.getPrincipalsRedisTemplate().boundSetOps(redisPrincipalKey);
        principalOps
            .members()
            .stream()
            .filter(value -> {
                val existingAccount = decode(value);
                return account.getId() == existingAccount.getId();
            })
            .findFirst()
            .ifPresent(principalOps::remove);
        principalOps.add(encodedAccount);
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

    /**
     * Delete the account record and its entry in the owner's principal set.
     * When the account record exists, it names the owner, so only that principal set is inspected.
     * When the record is already gone, the entry may still be in a principal set: before this was fixed,
     * deleting a device removed the record but left the set entry behind. The owner cannot be
     * determined from the id alone, so all principal sets are scanned in batches and any entry with
     * this id is removed. This repair happens only here, on an explicit delete of that id; reads never
     * modify the principal sets.
     *
     * @param id the account id
     */
    @Override
    public void delete(final long id) {
        val accountKey = RedisCompositeKey.forAccounts().withAccount(id).toKeyPattern();
        val account = casRedisTemplates.getAccountsRedisTemplate().boundValueOps(accountKey).get();
        if (account != null) {
            val principalKey = RedisCompositeKey.forPrincipals().withPrincipal(account).toKeyPattern();
            removePrincipalEntries(List.of(principalKey), id, List.of(accountKey));
        } else {
            LOGGER.debug("Account [{}] has no record; scanning principal keys for entries left behind", id);
            val principalKeyPattern = RedisCompositeKey.forPrincipals().toKeyPattern();
            try (val principalKeys = casRedisTemplates.getPrincipalsRedisTemplate().scan(principalKeyPattern)) {
                principalKeys
                    .gather(Gatherers.windowFixed(PRINCIPAL_KEYS_BATCH_SIZE))
                    .forEach(batch -> removePrincipalEntries(batch, id, List.of()));
            }
        }
    }

    @Override
    public long count() {
        val redisKeyPattern = RedisCompositeKey.forAccounts().toKeyPattern();
        return casRedisTemplates.getAccountsRedisTemplate().count(redisKeyPattern);
    }

    @Override
    public long count(final String username) {
        val redisKeyPattern = RedisCompositeKey.forPrincipals().withPrincipal(username).toKeyPattern();
        return Objects.requireNonNullElse(casRedisTemplates.getPrincipalsRedisTemplate().boundSetOps(redisKeyPattern).size(), 0L);
    }

    /**
     * Remove every member of the given principal sets that belongs to the account id, and delete the given keys.
     * This costs two round trips however many keys are given: one pipeline reads all sets, and a second
     * removes the matching members and deletes the keys. Members are read and removed as the raw bytes Redis
     * holds, so the removal matches the stored member exactly rather than depending on a deserialized copy
     * serializing back to the same bytes. Removals run before the deletes, so a set entry is never left
     * pointing at a deleted account record.
     *
     * @param principalKeys the principal keys whose sets are inspected
     * @param id            the account id whose entries are removed
     * @param keysToDelete  the keys to delete once the entries are removed
     */
    private void removePrincipalEntries(final List<String> principalKeys, final long id, final List<String> keysToDelete) {
        val principalsTemplate = casRedisTemplates.getPrincipalsRedisTemplate();
        val valueSerializer = Objects.requireNonNull(principalsTemplate.getValueSerializer());
        val principalSets = principalsTemplate.executePipelined((RedisCallback<Object>) connection -> {
            principalKeys.forEach(key -> connection.setCommands().sMembers(key.getBytes(StandardCharsets.UTF_8)));
            return null;
        }, RedisSerializer.byteArray());

        val removals = new LinkedHashMap<String, byte[][]>();
        for (var index = 0; index < principalKeys.size(); index++) {
            if (principalSets.get(index) instanceof Collection<?> members) {
                val matches = members.stream()
                    .map(byte[].class::cast)
                    .filter(member -> valueSerializer.deserialize(member) instanceof OneTimeTokenAccount account && account.getId() == id)
                    .toArray(byte[][]::new);
                if (matches.length > 0) {
                    removals.put(principalKeys.get(index), matches);
                }
            }
        }

        if (!removals.isEmpty() || !keysToDelete.isEmpty()) {
            principalsTemplate.executePipelined((RedisCallback<Object>) connection -> {
                removals.forEach((principalKey, members) -> {
                    LOGGER.debug("Removing account [{}] from principal key [{}]", id, principalKey);
                    connection.setCommands().sRem(principalKey.getBytes(StandardCharsets.UTF_8), members);
                });
                keysToDelete.forEach(key -> connection.keyCommands().del(key.getBytes(StandardCharsets.UTF_8)));
                return null;
            });
        }
    }

    @Data
    public static class CasRedisTemplates {
        private final CasRedisTemplate<String, OneTimeTokenAccount> accountsRedisTemplate;

        private final CasRedisTemplate<String, OneTimeTokenAccount> principalsRedisTemplate;
    }
}
