package org.apereo.cas.monitor;

import module java.base;
import org.apereo.cas.ha.ClusterMember;
import org.apereo.cas.ha.ClusterTopologyManager;
import org.apereo.cas.util.LdapConnectionFactoryRegistry;
import com.google.common.base.Splitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jspecify.annotations.Nullable;
import org.ldaptive.ActivePassiveConnectionStrategy;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionStrategy;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.LdapURL;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;

/**
 * The directory servers CAS's LDAP connection factories use, one member per server however many factories use it.
 * Each member says which factories use the server and how many of their pooled connections are on it, whether the
 * server answers now and when it last did not, and what the server says about itself: vendor and version, its naming
 * contexts, how far its replica has come, and for Active Directory its site, while the domain controllers it lists are
 * added as members too. Servers come from each factory's own connection strategy, so servers found through DNS SRV
 * records are listed as well. Each server is asked with the settings of a factory that uses it, with short timeouts and
 * all at once; the answer is kept briefly.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class LdapClusterTopologyManager implements ClusterTopologyManager {
    static final String ACTIVE_DIRECTORY_CAPABILITY = "1.2.840.113556.1.4.800";

    private static final int MAX_DOMAIN_CONTROLLERS = 50;

    private static final int MAX_NAMING_CONTEXTS = 5;

    private static final int PROBE_ROUNDS = 3;

    private static final int MAX_REASON_LENGTH = 200;

    private static final String[] ROOT_ATTRIBUTES = {
        "vendorName", "vendorVersion", "namingContexts", "defaultNamingContext", "configurationNamingContext",
        "dnsHostName", "serverName", "isSynchronized", "highestCommittedUSN", "isGlobalCatalogReady",
        "supportedCapabilities", "altServer"
    };

    private final Duration probeTimeout;

    private final Duration lifetime;

    private final Map<String, Instant> failures = new ConcurrentHashMap<>();

    private final Map<String, Integer> failureCounts = new ConcurrentHashMap<>();

    private volatile @Nullable Discovery lastDiscovery;

    /**
     * The site of an Active Directory server from the distinguished name of its server object, such as
     * CN=DC1,CN=Servers,CN=Headquarters,CN=Sites,CN=Configuration,DC=example,DC=org.
     *
     * @param dn the distinguished name
     * @return the site, or null
     */
    public static @Nullable String siteOf(final @Nullable String dn) {
        if (StringUtils.isBlank(dn)) {
            return null;
        }
        val parts = Splitter.on(',').trimResults().splitToList(dn);
        for (var index = 0; index < parts.size() - 1; index++) {
            if (Strings.CI.equals("CN=Servers", parts.get(index))) {
                return Strings.CI.removeStart(parts.get(index + 1), "CN=");
            }
        }
        return null;
    }

    /**
     * The servers a connection configuration reaches, as its connection strategy finds them: the URLs it is given, or
     * the servers DNS SRV records name. A fresh instance of the strategy is used, so the factory's own is not touched. When
     * the strategy cannot list them, the configured URLs are used, leaving out a DNS SRV lookup that could not be done.
     *
     * @param config the connection configuration
     * @return the servers
     */
    static List<LdapURL> serversOf(final ConnectionConfig config) {
        val urls = config.getLdapUrl();
        if (StringUtils.isBlank(urls)) {
            return List.of();
        }
        try {
            val strategy = Optional.ofNullable(config.getConnectionStrategy())
                .map(ConnectionStrategy::newInstance)
                .orElseGet(ActivePassiveConnectionStrategy::new);
            strategy.initialize(urls, _ -> true);
            val servers = new ArrayList<LdapURL>();
            strategy.iterator().forEachRemaining(servers::add);
            return List.copyOf(servers);
        } catch (final Exception e) {
            LOGGER.debug("Unable to list the servers of [{}]: [{}]", urls, e.getMessage());
            return Arrays.stream(StringUtils.split(urls))
                .filter(url -> !Strings.CI.startsWith(url, "dns:"))
                .map(LdapClusterTopologyManager::parse)
                .flatMap(Optional::stream)
                .toList();
        }
    }

    private static Optional<LdapURL> parse(final String url) {
        try {
            return Optional.of(new LdapURL(url));
        } catch (final Exception e) {
            LOGGER.debug("Skipping the LDAP URL [{}]: [{}]", url, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public String getName() {
        return "LDAP";
    }

    @Override
    public List<? extends ClusterMember> discoverMembers() {
        val previous = lastDiscovery;
        if (previous != null && previous.until().isAfter(Instant.now())) {
            return previous.members();
        }
        val members = discover();
        lastDiscovery = new Discovery(members, Instant.now().plus(lifetime));
        return members;
    }

    private List<ClusterMember> discover() {
        val servers = new TreeMap<String, Server>();
        val resolved = new HashMap<String, List<LdapURL>>();
        for (val registered : LdapConnectionFactoryRegistry.registered()) {
            val config = registered.factory().getConnectionConfig();
            val discovered = Strings.CI.startsWith(config.getLdapUrl(), "dns:") ? "DNS SRV" : "Configured";
            val strategy = Optional.ofNullable(config.getConnectionStrategy()).map(Object::getClass).map(Class::getName).orElse(StringUtils.EMPTY);
            for (val url : resolved.computeIfAbsent(config.getLdapUrl() + '|' + strategy, _ -> serversOf(config))) {
                servers.computeIfAbsent(LdapConnectionFactoryRegistry.serverKey(url), _ -> new Server(url, config, discovered))
                    .usedBy().add(registered.label());
            }
            Optional.ofNullable(registered.tracker())
                .ifPresent(tracker -> tracker.byServer().forEach((key, connections) -> {
                    val server = servers.get(key);
                    if (server != null) {
                        server.connections().put(registered.label(), connections);
                    }
                }));
        }
        val probes = probeAll(servers, true);
        val controllers = new TreeMap<String, Server>();
        servers.forEach((key, source) -> Optional.ofNullable(probes.get(key)).stream()
            .flatMap(probe -> probe.domainControllers().stream())
            .filter(host -> servers.values().stream().noneMatch(server -> Strings.CI.equals(server.url().getHostname(), host)))
            .limit(MAX_DOMAIN_CONTROLLERS)
            .forEach(host -> {
                val url = new LdapURL("%s://%s:%s".formatted(source.url().getScheme(), host, source.url().getPort()));
                controllers.computeIfAbsent(LdapConnectionFactoryRegistry.serverKey(url),
                    _ -> new Server(url, source.config(), "Active Directory"));
            }));
        probes.putAll(probeAll(controllers, false));
        servers.putAll(controllers);
        val positions = replicationPositions(probes);
        return servers.entrySet().stream()
            .map(entry -> member(entry.getKey(), entry.getValue(), probes.get(entry.getKey()), positions))
            .toList();
    }

    private Map<String, Probe> probeAll(final Map<String, Server> servers, final boolean findControllers) {
        val probes = new TreeMap<String, Probe>();
        try (val executor = Executors.newVirtualThreadPerTaskExecutor()) {
            val futures = new LinkedHashMap<String, Future<Probe>>();
            servers.forEach((key, server) -> futures.put(key, executor.submit(() -> probe(key, server, findControllers))));
            val deadline = System.nanoTime() + probeTimeout.multipliedBy(PROBE_ROUNDS).toNanos();
            futures.forEach((key, future) -> {
                try {
                    probes.put(key, future.get(Math.max(0, deadline - System.nanoTime()), TimeUnit.NANOSECONDS));
                } catch (final TimeoutException e) {
                    future.cancel(true);
                    probes.put(key, failed(key, probeTimeout.multipliedBy(PROBE_ROUNDS).toMillis(),
                        new IllegalStateException("The server took too long to answer")));
                    LOGGER.trace(e.getMessage(), e);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    probes.put(key, failed(key, 0, e));
                } catch (final Exception e) {
                    probes.put(key, failed(key, 0, e));
                }
            });
        }
        return probes;
    }

    private Probe probe(final String key, final Server server, final boolean findControllers) {
        val config = ConnectionConfig.copy(server.config());
        config.setLdapUrl(server.url().getHostnameWithSchemeAndPort());
        config.setConnectionStrategy(new ActivePassiveConnectionStrategy());
        config.setAutoReconnect(false);
        config.setConnectTimeout(shorter(config.getConnectTimeout()));
        config.setStartTLSTimeout(shorter(config.getStartTLSTimeout()));
        config.setResponseTimeout(shorter(config.getResponseTimeout()));
        val started = System.nanoTime();
        val factory = new DefaultConnectionFactory(config);
        try {
            val root = entry(factory, StringUtils.EMPTY, SearchScope.OBJECT, ROOT_ATTRIBUTES);
            if (root == null) {
                throw new IllegalStateException("The server did not return its root entry");
            }
            val elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
            val activeDirectory = values(root, "supportedCapabilities").contains(ACTIVE_DIRECTORY_CAPABILITY);
            val replication = activeDirectory ? Map.<String, List<String>>of() : contextCsns(factory, values(root, "namingContexts"));
            val controllers = activeDirectory && findControllers
                ? domainControllers(factory, value(root, "configurationNamingContext"))
                : List.<String>of();
            return new Probe(true, elapsed, root, replication, controllers, null);
        } catch (final Exception e) {
            return failed(key, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started), e);
        } finally {
            factory.close();
        }
    }

    private Probe failed(final String key, final long elapsed, final Exception error) {
        LOGGER.debug("LDAP server [{}] did not answer: [{}]", key, error.getMessage());
        failures.put(key, Instant.now());
        failureCounts.merge(key, 1, Integer::sum);
        return new Probe(false, elapsed, null, Map.of(), List.of(), reasonOf(error));
    }

    /**
     * A short reason for a failed probe, without the internals ldaptive puts in its messages: a refused bind shows its
     * result code and the server's diagnostic message, anything else the message of its root cause.
     *
     * @param error the failure
     * @return the reason
     */
    public static String reasonOf(final Throwable error) {
        val chain = ExceptionUtils.getThrowableList(error);
        val messages = chain.stream()
            .map(Throwable::getMessage)
            .filter(StringUtils::isNotBlank)
            .toList();
        val refused = messages.stream()
            .filter(message -> Strings.CS.startsWith(message, "Connection initializer") && Strings.CS.contains(message, "resultCode="))
            .findFirst();
        if (refused.isPresent()) {
            val code = StringUtils.substringBetween(refused.get(), "resultCode=", ",");
            val diagnostic = StringUtils.trimToNull(StringUtils.substringBetween(refused.get(), "diagnosticMessage=", ","));
            return "The server refused the bind"
                + Optional.ofNullable(StringUtils.trimToNull(code))
                    .map(value -> ": " + StringUtils.replaceChars(value, '_', ' ').toLowerCase(Locale.ENGLISH))
                    .orElse(StringUtils.EMPTY)
                + Optional.ofNullable(diagnostic).map(value -> " (" + value + ')').orElse(StringUtils.EMPTY);
        }
        val reason = messages.isEmpty() ? chain.getLast().getClass().getSimpleName() : messages.getLast();
        return StringUtils.abbreviate(reason, MAX_REASON_LENGTH);
    }

    private Duration shorter(final @Nullable Duration duration) {
        return duration == null || duration.isZero() || duration.compareTo(probeTimeout) > 0 ? probeTimeout : duration;
    }

    private static @Nullable LdapEntry entry(final DefaultConnectionFactory factory, final String dn,
                                             final SearchScope scope, final String... attributes)
            throws LdapException {
        val request = SearchRequest.builder()
            .dn(dn)
            .scope(scope)
            .filter("(objectClass=*)")
            .returnAttributes(attributes)
            .sizeLimit(1)
            .build();
        val response = new SearchOperation(factory).execute(request);
        return response.getResultCode() == ResultCode.SUCCESS ? response.getEntry() : null;
    }

    private static Map<String, List<String>> contextCsns(final DefaultConnectionFactory factory, final List<String> contexts) {
        val positions = new LinkedHashMap<String, List<String>>();
        contexts.stream().limit(MAX_NAMING_CONTEXTS).forEach(context -> {
            try {
                val entry = entry(factory, context, SearchScope.OBJECT, "contextCSN");
                val csns = entry == null ? List.<String>of() : values(entry, "contextCSN");
                if (!csns.isEmpty()) {
                    positions.put(context, csns.stream().sorted().toList());
                }
            } catch (final LdapException e) {
                LOGGER.trace("No replication position for [{}]: [{}]", context, e.getMessage());
            }
        });
        return positions;
    }

    private static List<String> domainControllers(final DefaultConnectionFactory factory, final @Nullable String configuration) {
        if (StringUtils.isBlank(configuration)) {
            return List.of();
        }
        try {
            val request = SearchRequest.builder()
                .dn("CN=Sites," + configuration)
                .scope(SearchScope.SUBTREE)
                .filter("(objectClass=server)")
                .returnAttributes("dNSHostName")
                .sizeLimit(MAX_DOMAIN_CONTROLLERS)
                .build();
            val response = new SearchOperation(factory).execute(request);
            return response.getEntries().stream()
                .map(entry -> value(entry, "dNSHostName"))
                .filter(StringUtils::isNotBlank)
                .map(host -> host.toLowerCase(Locale.ENGLISH))
                .distinct()
                .toList();
        } catch (final Exception e) {
            LOGGER.debug("Unable to list the domain controllers under [{}]: [{}]", configuration, e.getMessage());
            return List.of();
        }
    }

    /**
     * The server id of a change sequence number, such as 001 in 20261118120000.123456Z#000000#001#000000; a server
     * replicating from several providers holds one number per provider.
     *
     * @param csn the change sequence number
     * @return the server id, or the whole number when it has no server id
     */
    public static String providerOf(final String csn) {
        val parts = Splitter.on('#').splitToList(csn);
        return parts.size() > 2 ? parts.get(2) : csn;
    }

    /**
     * For each naming context and each provider, the latest change any server holds, so a server holding less for any
     * provider is behind.
     */
    private static Map<String, Map<String, String>> replicationPositions(final Map<String, Probe> probes) {
        val latest = new HashMap<String, Map<String, String>>();
        probes.values().forEach(probe -> probe.replication().forEach((context, csns) -> csns.forEach(csn ->
            latest.computeIfAbsent(context, _ -> new HashMap<>())
                .merge(providerOf(csn), csn, BinaryOperator.<String>maxBy(Comparator.naturalOrder())))));
        return latest;
    }

    private static boolean upToDate(final List<String> held, final @Nullable Map<String, String> latest) {
        if (latest == null) {
            return true;
        }
        val mine = held.stream()
            .collect(Collectors.toMap(LdapClusterTopologyManager::providerOf,
                Function.<String>identity(),
                BinaryOperator.<String>maxBy(Comparator.naturalOrder())));
        return latest.entrySet().stream().allMatch(entry -> entry.getValue().equals(mine.get(entry.getKey())));
    }

    private ClusterMember member(final String key, final Server server, final @Nullable Probe probe,
                                 final Map<String, Map<String, String>> positions) {
        val attributes = new LinkedHashMap<String, Object>();
        attributes.put("usedBy", server.usedBy().isEmpty() ? List.of("Not used by CAS") : List.copyOf(server.usedBy()));
        if (!server.connections().isEmpty()) {
            val connections = new TreeMap<String, String>();
            server.connections().forEach((label, count) ->
                connections.put(label, "%s open, %s in use".formatted(count.open(), count.taken())));
            attributes.put("connections", connections);
        }
        attributes.put("discovered", server.discovered());
        Optional.ofNullable(server.url().getInetAddress()).ifPresent(address -> attributes.put("ipAddress", address.getHostAddress()));
        if (server.url().getPriority() > 0) {
            attributes.put("priority", server.url().getPriority());
        }
        val root = probe != null ? probe.root() : null;
        var description = probe != null && probe.error() != null ? probe.error() : null;
        if (root != null) {
            Optional.ofNullable(value(root, "vendorName")).ifPresent(vendor -> attributes.put("vendor", vendor));
            Optional.ofNullable(value(root, "vendorVersion")).ifPresent(version -> attributes.put("version", version));
            val contexts = values(root, "namingContexts");
            if (!contexts.isEmpty()) {
                attributes.put("namingContexts", contexts);
            }
            val alternates = values(root, "altServer");
            if (!alternates.isEmpty()) {
                attributes.put("alternateServers", alternates);
            }
            if (values(root, "supportedCapabilities").contains(ACTIVE_DIRECTORY_CAPABILITY)) {
                val directory = new LinkedHashMap<String, Object>();
                Optional.ofNullable(siteOf(value(root, "serverName"))).ifPresent(site -> directory.put("site", site));
                Optional.ofNullable(value(root, "dnsHostName")).ifPresent(host -> directory.put("dnsHostName", host));
                Optional.ofNullable(value(root, "defaultNamingContext")).ifPresent(domain -> directory.put("domain", domain));
                Optional.ofNullable(value(root, "isSynchronized")).ifPresent(synced -> directory.put("synchronized", synced));
                Optional.ofNullable(value(root, "highestCommittedUSN")).ifPresent(usn -> directory.put("highestCommittedUSN", usn));
                Optional.ofNullable(value(root, "isGlobalCatalogReady")).ifPresent(catalog -> directory.put("globalCatalog", catalog));
                attributes.put("activeDirectory", directory);
                description = "Active Directory domain controller"
                    + Optional.ofNullable(directory.get("site")).map(site -> ", site " + site).orElse(StringUtils.EMPTY);
            } else {
                description = StringUtils.trimToNull("%s %s".formatted(
                    StringUtils.defaultString(value(root, "vendorName")), StringUtils.defaultString(value(root, "vendorVersion"))));
            }
        }
        if (probe != null && !probe.replication().isEmpty()) {
            val replication = new LinkedHashMap<String, Object>();
            probe.replication().forEach((context, csns) ->
                replication.put(context, Map.of("contextCSN", csns, "upToDate", upToDate(csns, positions.get(context)))));
            attributes.put("replication", replication);
        }
        Optional.ofNullable(failures.get(key)).ifPresent(failed -> {
            attributes.put("lastFailure", failed.toString());
            attributes.put("failures", failureCounts.getOrDefault(key, 0));
        });
        return ClusterMember.builder()
            .owner(getName())
            .id(key)
            .address(server.url().getHostnameWithPort())
            .status(probe != null && probe.success())
            .responseTime(probe != null ? probe.millis() : 0)
            .description(StringUtils.defaultString(description))
            .attributes(attributes)
            .build();
    }

    private static @Nullable String value(final LdapEntry entry, final String name) {
        val attribute = entry.getAttribute(name);
        return attribute != null ? attribute.getStringValue() : null;
    }

    private static List<String> values(final LdapEntry entry, final String name) {
        return Optional.ofNullable(entry.getAttribute(name))
            .map(LdapAttribute::getStringValues)
            .map(List::copyOf)
            .orElseGet(List::of);
    }

    private record Server(LdapURL url, ConnectionConfig config, String discovered, Set<String> usedBy,
        Map<String, LdapConnectionFactoryRegistry.ServerConnections> connections) {
        Server(final LdapURL url, final ConnectionConfig config, final String discovered) {
            this(url, config, discovered, new TreeSet<>(), new TreeMap<>());
        }
    }

    private record Probe(boolean success, long millis, @Nullable LdapEntry root, Map<String, List<String>> replication,
        List<String> domainControllers, @Nullable String error) {
    }

    private record Discovery(List<ClusterMember> members, Instant until) {
    }
}
