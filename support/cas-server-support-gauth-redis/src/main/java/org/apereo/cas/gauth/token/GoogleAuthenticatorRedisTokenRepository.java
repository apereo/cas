package org.apereo.cas.gauth.token;

import module java.base;
import org.apereo.cas.otp.repository.token.BaseOneTimeTokenRepository;
import org.apereo.cas.redis.core.CasRedisTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * This is {@link GoogleAuthenticatorRedisTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class GoogleAuthenticatorRedisTokenRepository extends BaseOneTimeTokenRepository<GoogleAuthenticatorToken> {
    private static final String KEY_SEPARATOR = ":";

    private static final String CAS_PREFIX = GoogleAuthenticatorRedisTokenRepository.class.getSimpleName();

    private final CasRedisTemplate<String, GoogleAuthenticatorToken> template;

    private final long expireTokensInSeconds;

    private final long scanCount;

    @Override
    public GoogleAuthenticatorToken store(final GoogleAuthenticatorToken token) {
        token.assignIdIfNecessary();
        val redisKey = getGoogleAuthenticatorTokenRedisKey(token);
        LOGGER.trace("Saving token [{}] using key [{}]", token, redisKey);
        val ops = this.template.boundValueOps(redisKey);
        ops.set(token);
        ops.expire(Duration.ofSeconds(this.expireTokensInSeconds));
        LOGGER.trace("Saved token [{}]", token);
        return token;
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
        try (val keys = getGoogleAuthenticatorTokenKeys()) {
            val redisKey = keys.collect(Collectors.toSet());
            LOGGER.trace("Deleting tokens using key [{}]", redisKey);
            this.template.delete(redisKey);
        }
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
        try (val keys = getGoogleAuthenticatorTokenKeys(uid)) {
            val redisKey = keys.collect(Collectors.toSet());
            LOGGER.trace("Deleting tokens for [{}] using key [{}]", uid, redisKey);
            this.template.delete(redisKey);
            LOGGER.trace("Deleted tokens [{}]", redisKey);
        }
    }

    @Override
    public void remove(final Integer otp) {
        try (val keys = getGoogleAuthenticatorTokenKeys(otp)) {
            val redisKey = keys.collect(Collectors.toSet());
            LOGGER.trace("Deleting token for [{}] using key [{}]", otp, redisKey);
            this.template.delete(redisKey);
            LOGGER.trace("Deleted tokens [{}]", redisKey);
        }
    }

    @Override
    public long count(final String uid) {
        try (val keys = getGoogleAuthenticatorTokenKeys(uid)) {
            return keys.count();
        }
    }

    @Override
    public long count() {
        try (val keys = getGoogleAuthenticatorTokenKeys()) {
            return keys.count();
        }
    }

    private static String getGoogleAuthenticatorTokenRedisKey(final GoogleAuthenticatorToken token) {
        return getGoogleAuthenticatorTokenRedisKey(token.getUserId(), token.getToken());
    }

    private static String getGoogleAuthenticatorTokenRedisKey(final String username, final Integer otp) {
        return CAS_PREFIX + KEY_SEPARATOR + username.trim().toLowerCase(Locale.ENGLISH) + KEY_SEPARATOR + otp;
    }

    private static String getGoogleAuthenticatorTokenRedisKey(final String username) {
        return CAS_PREFIX + KEY_SEPARATOR + username.trim().toLowerCase(Locale.ENGLISH) + KEY_SEPARATOR + '*';
    }

    private static String getGoogleAuthenticatorTokenRedisKey(final Integer otp) {
        return CAS_PREFIX + KEY_SEPARATOR + '*' + KEY_SEPARATOR + otp;
    }

    private static String getPatternGoogleAuthenticatorTokenRedisKey() {
        return CAS_PREFIX + KEY_SEPARATOR + '*';
    }

    private Stream<String> getGoogleAuthenticatorTokenKeys() {
        val key = getPatternGoogleAuthenticatorTokenRedisKey();
        LOGGER.trace("Fetching Google Authenticator records based on key [{}]", key);
        return template.scan(key, this.scanCount);
    }

    private Stream<String> getGoogleAuthenticatorTokenKeys(final String username) {
        val key = getGoogleAuthenticatorTokenRedisKey(username);
        LOGGER.trace("Fetching Google Authenticator records based on key [{}] for [{}]", key, username);
        return template.scan(key, this.scanCount);
    }

    private Stream<String> getGoogleAuthenticatorTokenKeys(final Integer otp) {
        val key = getGoogleAuthenticatorTokenRedisKey(otp);
        LOGGER.trace("Fetching Google Authenticator records based on key [{}] for [{}]", key, otp);
        return template.scan(key, this.scanCount);
    }

    @Override
    protected void cleanInternal() {
    }
}
