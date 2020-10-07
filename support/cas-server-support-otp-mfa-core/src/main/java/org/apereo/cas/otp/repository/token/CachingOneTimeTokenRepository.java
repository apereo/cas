package org.apereo.cas.otp.repository.token;

import org.apereo.cas.authentication.OneTimeToken;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * This is {@link CachingOneTimeTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class CachingOneTimeTokenRepository extends BaseOneTimeTokenRepository {
    private final Cache<String, Collection<OneTimeToken>> storage;

    @Override
    public void cleanInternal() {
        LOGGER.trace("Beginning to clean up the cache storage to remove expiring tokens");
        this.storage.cleanUp();
        LOGGER.debug("Estimated total of [{}] token(s) cached and may be removed in future iterations", this.storage.estimatedSize());
    }

    @Override
    public void store(final OneTimeToken token) {
        if (exists(token.getUserId(), token.getToken())) {
            val tokens = this.storage.getIfPresent(token.getUserId());
            Objects.requireNonNull(tokens).add(token);
            LOGGER.debug("Storing previously used tokens [{}] for user [{}]", tokens, token.getUserId());
            storage.put(token.getUserId(), tokens);
        } else {
            val tokens = new ArrayList<OneTimeToken>(1);
            tokens.add(token);
            LOGGER.debug("Storing new token [{}] for user [{}]", token, token.getUserId());
            storage.put(token.getUserId(), tokens);
        }
    }

    @Override
    public OneTimeToken get(final String uid, final Integer otp) {
        val tokens = storage.getIfPresent(uid);
        LOGGER.debug("Found used tokens [{}]", tokens);
        if (tokens != null) {
            return tokens
                .stream()
                .filter(t -> t.getToken().equals(otp))
                .findFirst()
                .orElse(null);
        }
        return null;
    }

    @Override
    public void remove(final String uid, final Integer otp) {
        val dataset = this.storage.asMap();
        LOGGER.debug("Locating user [{}] to remove token [{}]", uid, otp);
        if (dataset.containsKey(uid)) {
            val tokens = dataset.get(uid);
            tokens.removeIf(t -> otp.equals(t.getToken()));
            LOGGER.debug("Final collection of tokens for [{}] after removal are [{}]", uid, tokens);
            this.storage.put(uid, tokens);
        }
    }

    @Override
    public void remove(final String uid) {
        this.storage.invalidate(uid);
    }

    @Override
    public void remove(final Integer otp) {
        val dataset = this.storage.asMap();
        dataset.values().forEach(tokens -> tokens.removeIf(t -> otp.equals(t.getToken())));
    }

    @Override
    public void removeAll() {
        this.storage.invalidateAll();
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
