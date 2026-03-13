package org.apereo.cas.oidc.jwks.register;

import module java.base;
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
    public ClientJwksRegistrationEntry save(final String jkt, final String jwk) {
        val entry = new ClientJwksRegistrationEntry(jkt, jwk, Instant.now(Clock.systemUTC()));
        store.put(jkt, entry);
        return entry;
    }

    @Override
    public Optional<ClientJwksRegistrationEntry> findByJkt(final String jkt) {
        return Optional.ofNullable(store.get(jkt));
    }

    @Override
    public List<ClientJwksRegistrationEntry> load() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void removeByJkt(final String jkt) {
        store.remove(jkt);
    }

    @Override
    public void removeAll() {
        store.clear();
    }
}
