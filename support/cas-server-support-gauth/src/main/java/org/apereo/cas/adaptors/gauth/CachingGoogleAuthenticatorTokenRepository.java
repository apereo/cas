package org.apereo.cas.adaptors.gauth;

import com.google.common.cache.LoadingCache;
import org.apereo.cas.GoogleAuthenticatorToken;

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
        this.storage.cleanUp();
    }

    @Override
    public void store(final GoogleAuthenticatorToken token) {
        if (exists(token.getUserId(), token.getToken())) {
            try {
                final Collection<GoogleAuthenticatorToken> tokens = this.storage.get(token.getUserId());
                tokens.add(token);
                this.storage.put(token.getUserId(), tokens);
            } catch (final Exception e){
                logger.warn(e.getMessage(), e);
            }
        } else {
            final Collection<GoogleAuthenticatorToken> tokens = new ArrayList<>();
            tokens.add(token);
            this.storage.put(token.getUserId(), tokens);
        }
    }

    @Override
    public boolean exists(final String uid, final Integer otp) {
        try {
            final Collection<GoogleAuthenticatorToken> tokens = this.storage.get(uid);
            return tokens.stream().anyMatch(t -> t.getToken().equals(otp));
        } catch (final Exception e){
            logger.warn(e.getMessage(), e);
        }
        return false;
    }
}
