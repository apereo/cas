package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
    private static final String KEY_SEPARATOR = ":";

    private static final String CAS_PREFIX = RedisGoogleAuthenticatorTokenCredentialRepository.class.getSimpleName();

    private final RedisTemplate<String, List<? extends OneTimeTokenAccount>> template;

    public RedisGoogleAuthenticatorTokenCredentialRepository(final IGoogleAuthenticator googleAuthenticator,
                                                             final RedisTemplate<String, List<? extends OneTimeTokenAccount>> template,
                                                             final CipherExecutor<String, String> tokenCredentialCipher) {
        super(tokenCredentialCipher, googleAuthenticator);
        this.template = template;
    }

    @Override
    public OneTimeTokenAccount get(final String username, final long id) {
        val keys = getGoogleAuthenticatorTokenKeys(username, String.valueOf(id));
        if (keys != null && keys.size() == 1) {
            val r = this.template.boundValueOps(keys.iterator().next()).get();
            if (r != null && !r.isEmpty()) {
                return decode(r.get(0));
            }
        }
        return null;
    }

    @Override
    public OneTimeTokenAccount get(final long id) {
        val keys = getGoogleAuthenticatorTokenKeys("*", String.valueOf(id));
        if (keys != null && keys.size() == 1) {
            val r = this.template.boundValueOps(keys.iterator().next()).get();
            if (r != null && !r.isEmpty()) {
                return decode(r.get(0));
            }
        }
        return null;
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> get(final String username) {
        val keys = getGoogleAuthenticatorTokenKeys(username, "*");
        return keys
            .stream()
            .map(key -> this.template.boundValueOps(key).get())
            .filter(Objects::nonNull)
            .map(this::decode)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> load() {
        return (Collection) getGoogleAuthenticatorTokenKeys()
            .stream()
            .map(redisKey -> this.template.boundValueOps(redisKey).get())
            .filter(Objects::nonNull)
            .map(this::decode)
            .collect(Collectors.toList());
    }

    @Override
    public OneTimeTokenAccount save(final OneTimeTokenAccount account) {
        return update(account);
    }

    @Override
    public OneTimeTokenAccount update(final OneTimeTokenAccount account) {
        val encodedAccount = encode(account);
        val redisKey = getGoogleAuthenticatorRedisKey(account);
        LOGGER.trace("Saving [{}] using key [{}]", encodedAccount, redisKey);
        val ops = this.template.boundValueOps(redisKey);
        ops.set(CollectionUtils.wrapList(encodedAccount));
        return encodedAccount;
    }

    @Override
    public void deleteAll() {
        val redisKey = getGoogleAuthenticatorTokenKeys();
        LOGGER.trace("Deleting tokens using key [{}]", redisKey);
        this.template.delete(redisKey);
        LOGGER.trace("Deleted tokens");
    }

    @Override
    public void delete(final String username) {
        val redisKey = getGoogleAuthenticatorTokenKeys(username, "*");
        LOGGER.trace("Deleting tokens using key [{}]", redisKey);
        this.template.delete(redisKey);
        LOGGER.trace("Deleted tokens");
    }

    @Override
    public void delete(final long id) {
        val redisKey = getGoogleAuthenticatorTokenKeys("*", String.valueOf(id));
        LOGGER.trace("Deleting tokens using key [{}]", redisKey);
        this.template.delete(redisKey);
        LOGGER.trace("Deleted tokens");
    }

    @Override
    public long count() {
        val keys = getGoogleAuthenticatorTokenKeys();
        return keys.size();
    }

    @Override
    public long count(final String username) {
        val keys = getGoogleAuthenticatorTokenKeys(username, "*");
        return keys.size();
    }

    private static String getGoogleAuthenticatorRedisKey(final OneTimeTokenAccount account) {
        return CAS_PREFIX + KEY_SEPARATOR + account.getUsername().trim().toLowerCase() + KEY_SEPARATOR + account.getId();
    }

    private Set<String> getGoogleAuthenticatorTokenKeys(final String username, final String id) {
        val key = CAS_PREFIX + KEY_SEPARATOR + username.trim().toLowerCase() + KEY_SEPARATOR + id;
        LOGGER.trace("Fetching Google Authenticator records based on key [{}]", key);
        return this.template.keys(key);
    }

    private Set<String> getGoogleAuthenticatorTokenKeys() {
        val key = CAS_PREFIX + KEY_SEPARATOR + "*:*";
        LOGGER.trace("Fetching Google Authenticator records based on key [{}]", key);
        return this.template.keys(key);
    }
}
