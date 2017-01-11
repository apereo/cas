package org.apereo.cas.adaptors.gauth.repository.token;

import com.google.common.cache.LoadingCache;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link CachingGoogleAuthenticatorTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CachingGoogleAuthenticatorTokenRepository extends BaseGoogleAuthenticatorTokenRepository {
    private final LoadingCache<String, Collection<GoogleAuthenticatorToken>> storage;

    public CachingGoogleAuthenticatorTokenRepository(final LoadingCache<String, Collection<GoogleAuthenticatorToken>> storage) {
        this.storage = storage;
    }

    @Override
    public void clean() {
        logger.debug("Beginning to clean up the cache storage to remove expiring tokens");
        this.storage.cleanUp();
        logger.debug("Total of {} token(s) remain in the cache and may be removed in future iterations", this.storage.size());
    }

    @Override
    public void store(final GoogleAuthenticatorToken token) {
        if (exists(token.getUserId(), token.getToken())) {
            try {
                final Collection<GoogleAuthenticatorToken> tokens = this.storage.get(token.getUserId());
                tokens.add(token);

                logger.debug("Storing previously used tokens [{}] for user [{}]", tokens, token.getUserId());
                this.storage.put(token.getUserId(), tokens);
            } catch (final Exception e) {
                logger.warn(e.getMessage(), e);
            }
        } else {
            final Collection<GoogleAuthenticatorToken> tokens = new ArrayList<>();
            tokens.add(token);

            logger.debug("Storing previously used token [{}] for user [{}]", token, token.getUserId());
            this.storage.put(token.getUserId(), tokens);
        }
    }

    @Override
    public boolean exists(final String uid, final Integer otp) {
        try {
            final Collection<GoogleAuthenticatorToken> tokens = this.storage.getIfPresent(uid);
            logger.debug("Found used tokens {}", tokens);
            return tokens != null && tokens.stream().anyMatch(t -> t.getToken().equals(otp));
        } catch (final Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return false;
    }
}
