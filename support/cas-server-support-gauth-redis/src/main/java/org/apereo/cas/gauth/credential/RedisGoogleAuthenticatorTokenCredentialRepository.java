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
    private static final int SCAN_BATCH_SIZE = 500;

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
        return readPrincipalAccounts(username)
            .filter(account -> account.getId() == id)
            .findFirst()
            .map(this::decode)
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
        return readPrincipalAccounts(username)
            .map(this::decode)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Load every account. Account keys are found with {@code SCAN}, and their values are read with one {@code MGET}
     * per batch of keys rather than one {@code GET} per key.
     *
     * @return the decoded accounts
     */
    @Override
    public Collection<? extends OneTimeTokenAccount> load() {
        val accountsTemplate = casRedisTemplates.getAccountsRedisTemplate();
        try (val keys = accountsTemplate.scan(scanOptions(RedisCompositeKey.forAccounts().toKeyPattern()))) {
            return keys.stream()
                .distinct()
                .gather(Gatherers.windowFixed(SCAN_BATCH_SIZE))
                .map(batch -> accountsTemplate.opsForValue().multiGet(batch))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .map(this::decode)
                .collect(Collectors.toList());
        }
    }

    @Override
    public OneTimeTokenAccount save(final OneTimeTokenAccount account) {
        return update(account.assignIdIfNecessary());
    }

    /**
     * Store the account record and replace the account's entry in the owner's principal set. The set's members are
     * matched by id as stored, without being decoded, and the stale entry is removed as the raw bytes Redis holds.
     * The commands run one after another on the shared connection rather than in a pipeline: with Lettuce, a pipeline
     * takes a dedicated connection, which costs more than the few round trips it saves on a call made at every login.
     *
     * @param account the account
     * @return the encoded account
     */
    @Override
    @SuppressWarnings("unchecked")
    public OneTimeTokenAccount update(final OneTimeTokenAccount account) {
        val encodedAccount = encode(account);
        val accountKey = RedisCompositeKey.forAccounts().withAccount(encodedAccount).toKeyPattern().getBytes(StandardCharsets.UTF_8);
        val principalKey = RedisCompositeKey.forPrincipals().withPrincipal(encodedAccount).toKeyPattern().getBytes(StandardCharsets.UTF_8);
        LOGGER.trace("Saving account [{}]", encodedAccount);

        val accountSerializer = (RedisSerializer<OneTimeTokenAccount>) Objects.requireNonNull(casRedisTemplates.getAccountsRedisTemplate().getValueSerializer());
        val principalsTemplate = casRedisTemplates.getPrincipalsRedisTemplate();
        val principalSerializer = (RedisSerializer<OneTimeTokenAccount>) Objects.requireNonNull(principalsTemplate.getValueSerializer());
        val accountValue = Objects.requireNonNull(accountSerializer.serialize(encodedAccount));
        val principalValue = Objects.requireNonNull(principalSerializer.serialize(encodedAccount));
        principalsTemplate.execute((RedisCallback<Object>) connection -> {
            connection.stringCommands().set(accountKey, accountValue);
            val staleMembers = Objects.requireNonNullElseGet(connection.setCommands().sMembers(principalKey), Set::<byte[]>of)
                .stream()
                .filter(member -> isAccount(principalSerializer, member, encodedAccount.getId()))
                .toArray(byte[][]::new);
            if (staleMembers.length > 0) {
                connection.setCommands().sRem(principalKey, staleMembers);
            }
            connection.setCommands().sAdd(principalKey, principalValue);
            return null;
        });
        return encodedAccount;
    }

    @Override
    public void deleteAll() {
        var options = scanOptions(RedisCompositeKey.forAccounts().toKeyPattern());
        try (val result = casRedisTemplates.getAccountsRedisTemplate().scan(options)) {
            casRedisTemplates.getAccountsRedisTemplate().executePipelined((RedisCallback<Object>) connection -> {
                StreamSupport.stream(result.spliterator(), false)
                    .forEach(id -> connection.keyCommands().del(id.getBytes(StandardCharsets.UTF_8)));
                return null;
            });
        }
        options = scanOptions(RedisCompositeKey.forPrincipals().toKeyPattern());
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
            try (val principalKeys = casRedisTemplates.getPrincipalsRedisTemplate().scan(scanOptions(principalKeyPattern))) {
                principalKeys.stream()
                    .distinct()
                    .gather(Gatherers.windowFixed(SCAN_BATCH_SIZE))
                    .forEach(batch -> removePrincipalEntries(batch, id, List.of()));
            }
        }
    }

    /**
     * Count the account records with {@code SCAN}, asking for batches of keys rather than the server's default of ten,
     * and closing the cursor and its connection when done.
     *
     * @return the number of accounts
     */
    @Override
    public long count() {
        try (val keys = casRedisTemplates.getAccountsRedisTemplate().scan(scanOptions(RedisCompositeKey.forAccounts().toKeyPattern()))) {
            return keys.stream().count();
        }
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
                    .filter(member -> isAccount(valueSerializer, member, id))
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

    private static ScanOptions scanOptions(final String pattern) {
        return ScanOptions.scanOptions().match(pattern).count(SCAN_BATCH_SIZE).build();
    }

    private static boolean isAccount(final RedisSerializer<?> serializer, final byte[] member, final long id) {
        return serializer.deserialize(member) instanceof OneTimeTokenAccount account && account.getId() == id;
    }

    private Stream<OneTimeTokenAccount> readPrincipalAccounts(final String username) {
        val redisAccountKey = RedisCompositeKey.forPrincipals().withPrincipal(username).toKeyPattern();
        val accounts = casRedisTemplates.getPrincipalsRedisTemplate().boundSetOps(redisAccountKey).members();
        return Objects.requireNonNull(accounts).stream().filter(Objects::nonNull);
    }

    @Data
    public static class CasRedisTemplates {
        private final CasRedisTemplate<String, OneTimeTokenAccount> accountsRedisTemplate;

        private final CasRedisTemplate<String, OneTimeTokenAccount> principalsRedisTemplate;
    }
}
