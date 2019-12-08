package org.apereo.cas.otp.repository.token;

import org.apereo.cas.authentication.OneTimeToken;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

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
    public void removeAll() {
        this.storage.invalidateAll();
    }

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
                val tokens = this.storage.get(token.getUserId());
                tokens.add(token);

                LOGGER.debug("Storing previously used tokens [{}] for user [{}]", tokens, token.getUserId());
                this.storage.put(token.getUserId(), tokens);
            } catch (final Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        } else {
            val tokens = new ArrayList<OneTimeToken>(1);
            tokens.add(token);

            LOGGER.debug("Storing previously used token [{}] for user [{}]", token, token.getUserId());
            this.storage.put(token.getUserId(), tokens);
        }
    }

    @Override
    public OneTimeToken get(final String uid, final Integer otp) {
        try {
            val tokens = this.storage.getIfPresent(uid);
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

    @Override
    public void remove(final String uid, final Integer otp) {
        val dataset = this.storage.asMap();
        if (dataset.containsKey(uid)) {
            val tokens = dataset.get(uid);
            tokens.removeIf(t -> otp == t.getId());
            this.storage.put(uid, tokens);
            this.storage.refresh(uid);
        }
    }

    @Override
    public void remove(final String uid) {
        this.storage.invalidate(uid);
        this.storage.refresh(uid);
    }

    @Override
    public void remove(final Integer otp) {
        val dataset = this.storage.asMap();
        dataset.values().forEach(tokens -> tokens.removeIf(t -> otp == t.getId()));
    }

    @Override
    public long count(final String uid) {
        val tokens = this.storage.getIfPresent(uid);
        LOGGER.debug("Found used tokens [{}]", tokens);
        if (tokens != null) {
            return tokens.size();
        }
        return 0;
    }

    @Override
    public long count() {
        return this.storage.estimatedSize();
    }
}
