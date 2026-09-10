package org.apereo.cas.oidc.jwks.register;

import module java.base;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import lombok.val;

/**
 * This is {@link SimpleClientJwksRegistrationStore}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public class SimpleClientJwksRegistrationStore implements ClientJwksRegistrationStore {
    private static final int DEFAULT_MAXIMUM_SIZE = 1_000;

    private final CasReentrantLock lock = new CasReentrantLock();

    private final Map<String, ClientJwksRegistrationEntry> store =
        new LinkedHashMap<>(DEFAULT_MAXIMUM_SIZE, 0.75F, true);

    @Override
    public ClientJwksRegistrationEntry save(final String clientId, final String jkt, final String jwk) {
        val entry = new ClientJwksRegistrationEntry(jkt, clientId, jwk, Instant.now(Clock.systemUTC()));
        return executeLocked(() -> {
            store.put(buildKey(clientId, jkt), entry);
            if (store.size() > DEFAULT_MAXIMUM_SIZE) {
                store.remove(store.keySet().iterator().next());
            }
            return entry;
        });
    }

    @Override
    public Optional<ClientJwksRegistrationEntry> findBy(final String clientId, final String jkt) {
        return executeLocked(() -> Optional.ofNullable(store.get(buildKey(clientId, jkt))));
    }

    @Override
    public List<ClientJwksRegistrationEntry> load() {
        return executeLocked(() -> new ArrayList<>(store.values()));
    }

    @Override
    public void remove(final String clientId, final String jkt) {
        executeLocked(() -> {
            store.remove(buildKey(clientId, jkt));
        });
    }

    @Override
    public void removeAll() {
        executeLocked(store::clear);
    }

    private static String buildKey(final String clientId, final String jkt) {
        return DigestUtils.sha512(clientId + ':' + jkt);
    }

    private <T> T executeLocked(final Supplier<T> operation) {
        return Objects.requireNonNull(lock.tryLock(operation::get));
    }

    private void executeLocked(final Runnable operation) {
        lock.tryLock(operation::run);
    }
}
