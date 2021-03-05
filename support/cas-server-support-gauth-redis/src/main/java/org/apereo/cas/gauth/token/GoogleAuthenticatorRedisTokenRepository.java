package org.apereo.cas.gauth.token;

import org.apereo.cas.authentication.OneTimeToken;
import org.apereo.cas.otp.repository.token.BaseOneTimeTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Set;

/**
 * This is {@link GoogleAuthenticatorRedisTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class GoogleAuthenticatorRedisTokenRepository extends BaseOneTimeTokenRepository {
    private static final String KEY_SEPARATOR = ":";

    private static final String CAS_PREFIX = GoogleAuthenticatorRedisTokenRepository.class.getSimpleName();

    private final RedisTemplate<String, GoogleAuthenticatorToken> template;

    private final long expireTokensInSeconds;

    @Override
    public void store(final OneTimeToken token) {
        val gauthToken = (GoogleAuthenticatorToken) token;
        val redisKey = getGoogleAuthenticatorTokenRedisKey(gauthToken);
        LOGGER.trace("Saving token [{}] using key [{}]", token, redisKey);
        val ops = this.template.boundValueOps(redisKey);
        ops.set(gauthToken);
        ops.expire(Duration.ofSeconds(this.expireTokensInSeconds));
        LOGGER.trace("Saved token [{}]", token);
    }

    @Override
    public GoogleAuthenticatorToken get(final String uid, final Integer otp) {
        val redisKey = getGoogleAuthenticatorTokenRedisKey(uid, otp);
        val ops = this.template.boundValueOps(redisKey);
        LOGGER.trace("Locating token by identifier [{}] using key [{}]", uid, redisKey);
        return ops.get();
    }

    @Override
    public void removeAll() {
        val redisKey = getGoogleAuthenticatorTokenKeys();
        LOGGER.trace("Deleting tokens using key [{}]", redisKey);
        this.template.delete(redisKey);
    }

    @Override
    public void remove(final String uid, final Integer otp) {
        val redisKey = getGoogleAuthenticatorTokenRedisKey(uid, otp);
        LOGGER.trace("Deleting token [{}] for [{}] using key [{}]", otp, uid, redisKey);
        this.template.delete(redisKey);
        LOGGER.trace("Deleted token [{}]", redisKey);
    }

    @Override
    public void remove(final String uid) {
        val redisKey = getGoogleAuthenticatorTokenKeys(uid);
        LOGGER.trace("Deleting tokens for [{}] using key [{}]", uid, redisKey);
        this.template.delete(redisKey);
        LOGGER.trace("Deleted tokens [{}]", redisKey);
    }

    @Override
    public void remove(final Integer otp) {
        val redisKey = getGoogleAuthenticatorTokenKeys(otp);
        LOGGER.trace("Deleting token for [{}] using key [{}]", otp, redisKey);
        this.template.delete(redisKey);
        LOGGER.trace("Deleted tokens [{}]", redisKey);
    }

    @Override
    public long count(final String uid) {
        val keys = getGoogleAuthenticatorTokenKeys(uid);
        return keys.size();
    }

    @Override
    public long count() {
        val keys = getGoogleAuthenticatorTokenKeys();
        return keys.size();
    }

    private static String getGoogleAuthenticatorTokenRedisKey(final GoogleAuthenticatorToken token) {
        return getGoogleAuthenticatorTokenRedisKey(token.getUserId(), token.getToken());
    }

    private static String getGoogleAuthenticatorTokenRedisKey(final String username, final Integer otp) {
        return CAS_PREFIX + KEY_SEPARATOR + username.trim().toLowerCase() + KEY_SEPARATOR + otp;
    }

    private static String getGoogleAuthenticatorTokenRedisKey(final String username) {
        return CAS_PREFIX + KEY_SEPARATOR + username.trim().toLowerCase() + KEY_SEPARATOR + '*';
    }

    private static String getGoogleAuthenticatorTokenRedisKey(final Integer otp) {
        return CAS_PREFIX + KEY_SEPARATOR + '*' + KEY_SEPARATOR + otp;
    }

    private static String getPatternGoogleAuthenticatorTokenRedisKey() {
        return CAS_PREFIX + KEY_SEPARATOR + '*';
    }

    private Set<String> getGoogleAuthenticatorTokenKeys() {
        val key = getPatternGoogleAuthenticatorTokenRedisKey();
        LOGGER.trace("Fetching Google Authenticator records based on key [{}]", key);
        return this.template.keys(key);
    }

    private Set<String> getGoogleAuthenticatorTokenKeys(final String username) {
        val key = getGoogleAuthenticatorTokenRedisKey(username);
        LOGGER.trace("Fetching Google Authenticator records based on key [{}] for [{}]", key, username);
        return this.template.keys(key);
    }

    private Set<String> getGoogleAuthenticatorTokenKeys(final Integer otp) {
        val key = getGoogleAuthenticatorTokenRedisKey(otp);
        LOGGER.trace("Fetching Google Authenticator records based on key [{}] for [{}]", key, otp);
        return this.template.keys(key);
    }

    @Override
    protected void cleanInternal() {
    }
}
