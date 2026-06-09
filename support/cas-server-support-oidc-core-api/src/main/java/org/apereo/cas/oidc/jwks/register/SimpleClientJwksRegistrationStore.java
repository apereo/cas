package org.apereo.cas.oidc.jwks.register;

import module java.base;
import org.apereo.cas.util.DigestUtils;
import lombok.val;

/**
 * This is {@link SimpleClientJwksRegistrationStore}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public class SimpleClientJwksRegistrationStore implements ClientJwksRegistrationStore {
    private final Map<String, ClientJwksRegistrationEntry> store = new ConcurrentHashMap<>();

    @Override
    public ClientJwksRegistrationEntry save(final String clientId, final String jkt, final String jwk) {
        val entry = new ClientJwksRegistrationEntry(jkt, clientId, jwk, Instant.now(Clock.systemUTC()));
        store.put(buildKey(clientId, jkt), entry);
        return entry;
    }

    @Override
    public Optional<ClientJwksRegistrationEntry> findBy(final String clientId, final String jkt) {
        return Optional.ofNullable(store.get(buildKey(clientId, jkt)));
    }

    @Override
    public List<ClientJwksRegistrationEntry> load() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void remove(final String clientId, final String jkt) {
        store.remove(buildKey(clientId, jkt));
    }

    @Override
    public void removeAll() {
        store.clear();
    }

    private static String buildKey(final String clientId, final String jkt) {
        return DigestUtils.sha512(clientId + ':' + jkt);
    }
}
