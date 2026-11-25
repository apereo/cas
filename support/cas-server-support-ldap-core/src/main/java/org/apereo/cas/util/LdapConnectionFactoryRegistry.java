package org.apereo.cas.util;

import module java.base;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapURL;
import org.ldaptive.PooledConnectionFactory;

/**
 * The LDAP connection factories CAS creates, each with what it was made for and, for a pool, the server each of its
 * connections is on. Factories are held weakly, so one that is discarded, such as on a refresh, goes with it.
 * A pool learns its connections through its activator and passivator: the passivator sees every new connection and
 * every connection put back, the activator every connection taken out.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@UtilityClass
public class LdapConnectionFactoryRegistry {
    private static final Queue<Registration> REGISTRATIONS = new ConcurrentLinkedQueue<>();

    /**
     * Remember a connection factory, and follow the connections of a pool. A pool must not be initialized yet.
     *
     * @param factory    the factory
     * @param properties the settings it was made from
     */
    public static void register(final ConnectionFactory factory, final AbstractLdapProperties properties) {
        REGISTRATIONS.removeIf(Registration::isGone);
        val tracker = factory instanceof final PooledConnectionFactory pool ? follow(pool) : null;
        REGISTRATIONS.add(new Registration(new WeakReference<>(factory), label(properties), tracker));
    }

    /**
     * The factories still in use; a pool that was closed is left out.
     *
     * @return the factories, with their labels and connections
     */
    public static List<RegisteredFactory> registered() {
        REGISTRATIONS.removeIf(Registration::isGone);
        return REGISTRATIONS.stream()
            .flatMap(registration -> Optional.ofNullable(registration.factory().get())
                .filter(factory -> !(factory instanceof final PooledConnectionFactory pool) || pool.isInitialized())
                .map(factory -> new RegisteredFactory(factory, registration.label(), registration.tracker()))
                .stream())
            .toList();
    }

    /**
     * The key of a server, its scheme, host and port, such as ldaps://dc1.example.org:636.
     *
     * @param url the server
     * @return the key
     */
    public static String serverKey(final LdapURL url) {
        return url.getHostnameWithSchemeAndPort().toLowerCase(Locale.ENGLISH);
    }

    /**
     * A name for a factory from the settings it was made from, such as "LDAP Authentication (corporate)".
     *
     * @param properties the settings
     * @return the label
     */
    static String label(final AbstractLdapProperties properties) {
        val type = StringUtils.defaultIfBlank(Arrays.stream(StringUtils.splitByCharacterTypeCamelCase(
                Strings.CS.removeEnd(properties.getClass().getSimpleName(), "Properties")))
            .map(word -> Strings.CI.equals(word, "Ldap") ? "LDAP" : word)
            .collect(Collectors.joining(" ")), "LDAP");
        val name = StringUtils.normalizeSpace(properties.getName());
        return StringUtils.isBlank(name) ? type : "%s (%s)".formatted(type, name);
    }

    private static ConnectionTracker follow(final PooledConnectionFactory pool) {
        val tracker = new ConnectionTracker(Math.max(pool.getMaxPoolSize(), 1) << 1);
        val activator = pool.getActivator();
        pool.setActivator(connection -> {
            val activated = activator.apply(connection);
            if (Boolean.TRUE.equals(activated)) {
                tracker.seen(connection, true);
            }
            return activated;
        });
        val passivator = pool.getPassivator();
        pool.setPassivator(connection -> {
            val passivated = passivator.apply(connection);
            if (Boolean.TRUE.equals(passivated)) {
                tracker.seen(connection, false);
            }
            return passivated;
        });
        return tracker;
    }

    private record Registration(WeakReference<ConnectionFactory> factory, String label, @Nullable ConnectionTracker tracker) {
        boolean isGone() {
            return factory.get() == null;
        }
    }

    /**
     * A factory in use.
     *
     * @param factory the factory
     * @param label   what it was made for
     * @param tracker its connections, for a pool
     */
    public record RegisteredFactory(ConnectionFactory factory, String label, @Nullable ConnectionTracker tracker) {
    }

    /**
     * The open connections of a pool, each with whether it is taken out. Closed connections are dropped once there are
     * more than the pool can hold, so the bookkeeping stays bounded.
     */
    @RequiredArgsConstructor
    public static final class ConnectionTracker {
        private final Map<Connection, Boolean> connections = new ConcurrentHashMap<>();

        private final int bound;

        void seen(final Connection connection, final boolean taken) {
            connections.put(connection, taken);
            if (connections.size() > bound) {
                connections.keySet().removeIf(Predicate.not(Connection::isOpen));
            }
        }

        /**
         * The open connections per server, such as ldaps://dc1.example.org:636.
         *
         * @return per server, how many connections are open and how many of those are taken out
         */
        public Map<String, ServerConnections> byServer() {
            connections.keySet().removeIf(Predicate.not(Connection::isOpen));
            val servers = new TreeMap<String, ServerConnections>();
            connections.forEach((connection, taken) -> {
                val url = connection.getLdapURL();
                if (url != null) {
                    servers.merge(serverKey(url), new ServerConnections(1, taken ? 1 : 0), ServerConnections::plus);
                }
            });
            return servers;
        }
    }

    /**
     * The connections of a pool on one server.
     *
     * @param open  how many are open
     * @param taken how many of those are taken out
     */
    public record ServerConnections(int open, int taken) {
        ServerConnections plus(final ServerConnections other) {
            return new ServerConnections(open + other.open(), taken + other.taken());
        }
    }
}
