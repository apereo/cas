package org.apereo.cas.oidc.jwks.register;

import module java.base;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.springframework.util.Assert;

/**
 * This is {@link SimpleClientJwksRegistrationNonceStore}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public class SimpleClientJwksRegistrationNonceStore implements ClientJwksRegistrationNonceStore {
    private static final int NONCE_LENGTH = 64;
    private static final int NONCE_EXPIRATION_SECONDS = 120;

    private final Map<String, ClientJwksRegistrationNonceEntry> store = new ConcurrentHashMap<>();

    @Override
    public ClientJwksChallengeResponse create() {
        val bytes = new byte[NONCE_LENGTH];
        RandomUtils.getNativeInstance().nextBytes(bytes);
        val nonceId = UUID.randomUUID().toString();
        val entry = new ClientJwksRegistrationNonceEntry(
            nonceId,
            Base64.getUrlEncoder().withoutPadding().encodeToString(bytes),
            Instant.now(Clock.systemUTC()).plusSeconds(NONCE_EXPIRATION_SECONDS));
        store.put(nonceId, entry);
        return new ClientJwksChallengeResponse(nonceId, entry.nonce());
    }

    @Override
    public ClientJwksRegistrationNonceEntry find(final String nonceId) {
        val entry = store.get(nonceId);
        Assert.isTrue(entry != null && Instant.now(Clock.systemUTC()).isBefore(entry.expiration()),
            String.format("Nonce [%s] is invalid or has expired", nonceId));
        return entry;
    }

    @Override
    public void remove(final String nonceId) {
        store.remove(nonceId);
    }
}
