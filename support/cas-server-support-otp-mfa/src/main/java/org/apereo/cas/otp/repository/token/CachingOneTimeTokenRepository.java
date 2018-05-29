package org.apereo.cas.otp.repository.token;


import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.OneTimeToken;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link CachingOneTimeTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class CachingOneTimeTokenRepository extends BaseOneTimeTokenRepository {
    private final LoadingCache<String, Collection<OneTimeToken>> storage;

    @Override
    public void cleanInternal() {
        LOGGER.debug("Beginning to clean up the cache storage to remove expiring tokens");
        this.storage.cleanUp();
        LOGGER.debug("Estimated total of [{}] token(s) remain in the cache and may be removed in future iterations", this.storage.estimatedSize());
    }

    @Override
    public void store(final OneTimeToken token) {
        if (exists(token.getUserId(), token.getToken())) {
            try {
                final var tokens = this.storage.get(token.getUserId());
                tokens.add(token);

                LOGGER.debug("Storing previously used tokens [{}] for user [{}]", tokens, token.getUserId());
                this.storage.put(token.getUserId(), tokens);
            } catch (final Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        } else {
            final Collection<OneTimeToken> tokens = new ArrayList<>();
            tokens.add(token);

            LOGGER.debug("Storing previously used token [{}] for user [{}]", token, token.getUserId());
            this.storage.put(token.getUserId(), tokens);
        }
    }

    @Override
    public OneTimeToken get(final String uid, final Integer otp) {
        try {
            final var tokens = this.storage.getIfPresent(uid);
            LOGGER.debug("Found used tokens [{}]", tokens);
            if (tokens != null) {
                return tokens
                        .stream()
                        .filter(t -> t.getToken().equals(otp))
                        .findFirst()
                        .orElse(null);
            }
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return null;
    }
}
