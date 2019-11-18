package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.redis.core.RedisTemplate;

import javax.persistence.NoResultException;
import java.util.ArrayList;
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

    private final RedisTemplate template;

    public RedisGoogleAuthenticatorTokenCredentialRepository(final IGoogleAuthenticator googleAuthenticator,
                                                             final RedisTemplate template,
                                                             final CipherExecutor<String, String> tokenCredentialCipher) {
        super(tokenCredentialCipher, googleAuthenticator);
        this.template = template;
    }

    @Override
    public OneTimeTokenAccount get(final String username) {
        try {
            val redisKey = getGoogleAuthenticatorRedisKey(username);
            val ops = this.template.boundValueOps(redisKey);
            val r = (OneTimeTokenAccount) ops.get();
            if (r != null) {
                return decode(r);
            }
        } catch (final NoResultException e) {
            LOGGER.debug("No record could be found for google authenticator id [{}]", username);
        }
        return null;
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> load() {
        try {
            return getGoogleAuthenticatorTokenKeys()
                .stream()
                .map(redisKey -> this.template.boundValueOps(redisKey).get())
                .filter(Objects::nonNull)
                .map(r -> (OneTimeTokenAccount) r)
                .map(this::decode)
                .collect(Collectors.toList());
        } catch (final Exception e) {
            LOGGER.error("No record could be found for google authenticator", e);
        }
        return new ArrayList<>(0);
    }

    @Override
    public void save(final String userName, final String secretKey, final int validationCode, final List<Integer> scratchCodes) {
        val account = new GoogleAuthenticatorAccount(userName, secretKey, validationCode, scratchCodes);
        update(account);
    }

    @Override
    public OneTimeTokenAccount update(final OneTimeTokenAccount account) {
        val encodedAccount = encode(account);

        val redisKey = getGoogleAuthenticatorRedisKey(account.getUsername());
        LOGGER.trace("Saving [{}] using key [{}]", encodedAccount, redisKey);
        val ops = this.template.boundValueOps(redisKey);
        ops.set(encodedAccount);

        return encodedAccount;
    }

    @Override
    public void deleteAll() {
        try {
            val redisKey = getGoogleAuthenticatorTokenKeys();
            LOGGER.trace("Deleting tokens using key [{}]", redisKey);
            this.template.delete(redisKey);
            LOGGER.trace("Deleted tokens");
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    @Override
    public void delete(final String username) {
        try {
            val redisKey = getGoogleAuthenticatorTokenKeys(username);
            LOGGER.trace("Deleting tokens using key [{}]", redisKey);
            this.template.delete(redisKey);
            LOGGER.trace("Deleted tokens");
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    @Override
    public long count() {
        try {
            val keys = getGoogleAuthenticatorTokenKeys();
            return keys.size();
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return 0;
    }

    private static String getGoogleAuthenticatorRedisKey(final String username) {
        return CAS_PREFIX + KEY_SEPARATOR + username;
    }

    private Set<String> getGoogleAuthenticatorTokenKeys(final String username) {
        val key = CAS_PREFIX + KEY_SEPARATOR + username;
        LOGGER.trace("Fetching Google Authenticator records based on key [{}]", key);
        return this.template.keys(key);
    }

    private Set<String> getGoogleAuthenticatorTokenKeys() {
        val key = CAS_PREFIX + KEY_SEPARATOR + '*';
        LOGGER.trace("Fetching Google Authenticator records based on key [{}]", key);
        return this.template.keys(key);
    }
}
