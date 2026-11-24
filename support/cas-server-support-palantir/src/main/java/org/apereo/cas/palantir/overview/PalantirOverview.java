package org.apereo.cas.palantir.overview;

import module java.base;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicesDeletedEvent;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CasVersion;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.InetAddressUtils;
import com.sun.management.OperatingSystemMXBean;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.math.NumberUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.health.contributor.CompositeHealthContributor;
import org.springframework.boot.health.contributor.HealthContributor;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.health.contributor.Status;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.lang.management.ManagementFactory;

/**
 * The summary behind Palantir's home page: the server, its health checks and resources, HTTP traffic and runtime
 * metrics, single sign-on sessions, applications by type and the latest changes to applications, leaving out the
 * services CAS registers for its own use.
 * <p>
 * Changes to applications come from the service change history when it is installed, which every CAS server shares,
 * so the list covers the whole cluster; otherwise they are remembered from the events this server publishes when it
 * saves or deletes one, since it started. Neither reads the audit trail.
 * <p>
 * Every section is computed on its own, in parallel, and kept for a while, so that one answer serves every
 * administrator who has the page open and the sources are asked at most once per section and interval. A section
 * that takes too long answers what it had last time, marked as stale, and finishes in the background; a section is
 * only computed when the actuator endpoint that shows the same data is exposed, so the page never shows what the
 * deployment has turned off.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
public class PalantirOverview implements DisposableBean {
    private static final Duration SECTION_TIMEOUT = Duration.ofSeconds(3);

    private static final Duration HEALTH_INDICATOR_TIMEOUT = Duration.ofMillis(2500);

    private static final List<String> DEFAULT_HEALTH_STATUS_ORDER = List.of(Status.DOWN.getCode(), Status.OUT_OF_SERVICE.getCode(),
        Status.UP.getCode(), Status.UNKNOWN.getCode());

    private static final int DEFAULT_LIFETIME = 30;

    private static final int EXPIRY_WARNING_DAYS = 14;

    private static final int RECENT_CHANGES = 20;

    private static final Duration DUPLICATE_CHANGE_WINDOW = Duration.ofSeconds(5);

    private static final String SYSTEM_AUTHOR = "CAS";

    private static final int SLOWEST_ENDPOINTS = 10;

    private static final List<String> ENDPOINTS_LEFT_OUT = List.of("/actuator", "/palantir", "/webjars", "/js/", "/css/",
        "/images/", "/favicon", "/themes/");

    private final ConfigurableApplicationContext applicationContext;

    private final ObjectProvider<ServicesManager> servicesManager;

    private final ObjectProvider<TicketRegistry> ticketRegistry;

    private final ObjectProvider<MeterRegistry> meterRegistry;

    private final ObjectProvider<PalantirApplicationChanges> sharedChanges;

    private final AtomicReference<List<PalantirApplicationChanges.Change>> changes = new AtomicReference<>(List.of());

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private final Map<String, CachedSection> cache = new ConcurrentHashMap<>();

    private final Map<String, RunningSection> running = new ConcurrentHashMap<>();

    private final Map<String, Instant> invalidated = new ConcurrentHashMap<>();

    private final Map<String, HealthCheck> healthChecks = new ConcurrentHashMap<>();

    private final List<Section> sections;

    public PalantirOverview(final ConfigurableApplicationContext applicationContext,
                            final ObjectProvider<ServicesManager> servicesManager,
                            final ObjectProvider<TicketRegistry> ticketRegistry,
                            final ObjectProvider<MeterRegistry> meterRegistry,
                            final ObjectProvider<PalantirApplicationChanges> sharedChanges) {
        this.applicationContext = applicationContext;
        this.servicesManager = servicesManager;
        this.ticketRegistry = ticketRegistry;
        this.meterRegistry = meterRegistry;
        this.sharedChanges = sharedChanges;
        this.sections = List.of(
            new Section("server", Set.of(), false, Duration.ZERO, this::server),
            new Section("health", Set.of("health"), false, Duration.ofSeconds(DEFAULT_LIFETIME), this::health),
            new Section("resources", Set.of("metrics"), false, Duration.ZERO, PalantirOverview::resources),
            new Section("tickets", Set.of("ticketRegistry", "statistics", "ssoSessions"), true, Duration.ofSeconds(DEFAULT_LIFETIME), this::tickets),
            new Section("traffic", Set.of("metrics"), true, Duration.ZERO, this::traffic),
            new Section("endpoints", Set.of("metrics"), true, Duration.ZERO, this::endpoints),
            new Section("applications", Set.of("registeredServices"), true, Duration.ofSeconds(DEFAULT_LIFETIME), this::applications),
            new Section("changes", Set.of("registeredServices"), true, Duration.ofSeconds(DEFAULT_LIFETIME), this::changes)
        );
    }

    /**
     * Summarize the server for the home page, every section that applies.
     *
     * @param exposedEndpoints the ids of the actuator endpoints this deployment exposes
     * @param administrator    whether the person asking is an administrator; others see the server, its health and resources only
     * @return the sections that apply and the moment they were put together
     */
    public Map<String, Object> summarize(final Set<String> exposedEndpoints, final boolean administrator) {
        return summarize(exposedEndpoints, administrator, Set.of());
    }

    /**
     * Summarize the server for the home page, only the sections asked for, so a section nobody looks at, such as one
     * that is slow to compute, is not computed for them.
     *
     * @param exposedEndpoints the ids of the actuator endpoints this deployment exposes
     * @param administrator    whether the person asking is an administrator; others see the server, its health and resources only
     * @param wanted           the sections asked for; empty for every section
     * @return the sections that apply and the moment they were put together
     */
    public Map<String, Object> summarize(final Set<String> exposedEndpoints, final boolean administrator, final Set<String> wanted) {
        return summarize(exposedEndpoints, administrator, wanted, false);
    }

    /**
     * Summarize the server for the home page, only the sections asked for. A fresh summary does not use the values
     * kept from earlier, such as when the operator asks for a refresh; a section being computed already is waited for
     * rather than computed again.
     *
     * @param exposedEndpoints the ids of the actuator endpoints this deployment exposes
     * @param administrator    whether the person asking is an administrator; others see the server, its health and resources only
     * @param wanted           the sections asked for; empty for every section
     * @param fresh            whether to compute the sections again rather than use the values kept from earlier
     * @return the sections that apply and the moment they were put together
     */
    public Map<String, Object> summarize(final Set<String> exposedEndpoints, final boolean administrator,
                                         final Set<String> wanted, final boolean fresh) {
        val exposed = exposedEndpoints.stream().map(id -> id.toLowerCase(Locale.ENGLISH)).collect(Collectors.toSet());
        val applicable = sections.stream()
            .filter(section -> wanted.isEmpty() || wanted.contains(section.name()))
            .filter(section -> administrator || !section.administratorsOnly())
            .filter(section -> section.endpoints().isEmpty()
                || section.endpoints().stream().anyMatch(id -> exposed.contains(id.toLowerCase(Locale.ENGLISH))))
            .toList();
        val futures = new LinkedHashMap<String, CompletableFuture<Map<String, Object>>>();
        applicable.forEach(section -> futures.put(section.name(), start(section, fresh)));
        val deadline = System.nanoTime() + SECTION_TIMEOUT.toNanos();
        val result = new LinkedHashMap<String, Object>();
        futures.forEach((name, future) -> result.put(name, await(name, future, deadline)));
        val summary = new LinkedHashMap<String, Object>();
        summary.put("generated", Instant.now().toString());
        summary.put("sections", result);
        return summary;
    }

    @Override
    public void destroy() {
        executor.shutdownNow();
    }

    /**
     * Answer a section from the cache while it is fresh, or join or start its computation. A section whose data
     * changed after its cached value or its running computation began, such as the recent changes after an
     * application is saved, is computed again, so the change is not hidden until the cached value expires.
     *
     * @param section the section
     * @param fresh   whether to compute it again even when a value kept from earlier is still current
     * @return the section's value, now or once computed
     */
    private CompletableFuture<Map<String, Object>> start(final Section section, final boolean fresh) {
        val name = section.name();
        val changedAt = invalidated.getOrDefault(name, Instant.MIN);
        val cached = cache.get(name);
        if (!fresh && cached != null && !section.lifetime().isZero()
            && cached.started().isAfter(changedAt)
            && cached.computed().plus(section.lifetime()).isAfter(Instant.now())) {
            return CompletableFuture.completedFuture(cached.value());
        }
        return running.compute(name, (key, current) -> {
            if (current != null && !current.result().isDone() && current.started().isAfter(changedAt)) {
                return current;
            }
            val started = Instant.now();
            val result = CompletableFuture.supplyAsync(section.supplier(), executor)
                .whenComplete((value, error) -> {
                    if (value != null) {
                        cache.put(key, new CachedSection(value, Instant.now(), started));
                    } else {
                        LOGGER.debug("Overview section [{}] could not be computed: [{}]", key, String.valueOf(error));
                    }
                });
            return new RunningSection(started, result);
        }).result();
    }

    private Map<String, Object> await(final String name, final CompletableFuture<Map<String, Object>> future, final long deadline) {
        try {
            val remaining = Math.max(0, deadline - System.nanoTime());
            return future.get(remaining, TimeUnit.NANOSECONDS);
        } catch (final TimeoutException e) {
            val cached = cache.get(name);
            if (cached != null) {
                val stale = new LinkedHashMap<>(cached.value());
                stale.put("stale", true);
                stale.put("computed", cached.computed().toString());
                return stale;
            }
            return Map.of("pending", true);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return Map.of("unavailable", true);
        } catch (final Exception e) {
            LOGGER.debug("Overview section [{}] failed: [{}]", name, e.getMessage());
            return Map.of("unavailable", true);
        }
    }

    private Map<String, Object> server() {
        val runtime = ManagementFactory.getRuntimeMXBean();
        val server = new LinkedHashMap<String, Object>();
        server.put("version", StringUtils.defaultString(CasVersion.getVersion()));
        server.put("host", InetAddressUtils.getCasServerHostName());
        server.put("java", Runtime.version().toString());
        server.put("started", Instant.ofEpochMilli(runtime.getStartTime()).toString());
        server.put("uptimeSeconds", runtime.getUptime() / 1000);
        return server;
    }

    private Map<String, Object> health() {
        val checks = new TreeMap<String, HealthCheck>();
        applicationContext.getBeansOfType(HealthIndicator.class).entrySet().stream()
            .filter(entry -> !ScopedProxyUtils.isScopedTarget(entry.getKey()))
            .forEach(entry -> checks.put(healthCheckName(entry.getKey()),
                healthCheck(healthCheckName(entry.getKey()), () -> entry.getValue().health().getStatus().getCode())));
        applicationContext.getBeansOfType(CompositeHealthContributor.class).entrySet().stream()
            .filter(entry -> !ScopedProxyUtils.isScopedTarget(entry.getKey()))
            .filter(entry -> entry.getValue().stream().findAny().isPresent())
            .forEach(entry -> checks.put(healthCheckName(entry.getKey()),
                healthCheck(healthCheckName(entry.getKey()), () -> statusOf(entry.getValue(), healthStatusOrder()))));
        val deadline = System.nanoTime() + HEALTH_INDICATOR_TIMEOUT.toNanos();
        val components = new ArrayList<Map<String, Object>>();
        checks.forEach((name, check) -> {
            var status = "UNKNOWN";
            var millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - check.started());
            try {
                val result = check.result().get(Math.max(0, deadline - System.nanoTime()), TimeUnit.NANOSECONDS);
                status = result.status();
                millis = result.millis();
            } catch (final TimeoutException e) {
                status = "SLOW";
                millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - check.started());
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (final Exception e) {
                status = "DOWN";
            }
            val component = new LinkedHashMap<String, Object>();
            component.put("name", name);
            component.put("status", status);
            component.put("millis", millis);
            components.add(component);
        });
        val health = new LinkedHashMap<String, Object>();
        health.put("status", overallStatus(components.stream().map(component -> String.valueOf(component.get("status"))).toList(),
            healthStatusOrder()));
        health.put("components", components);
        return health;
    }

    /**
     * The overall health, as the health endpoint puts it together: the first status, in the configured order, that a
     * check has. A check that has not answered in time counts as unknown, and statuses the order does not name are left
     * out; with no check, the server is up.
     *
     * @param statuses the statuses of the checks
     * @param order    the status codes, worst first
     * @return the overall status
     */
    public static String overallStatus(final List<String> statuses, final List<String> order) {
        if (statuses.isEmpty()) {
            return Status.UP.getCode();
        }
        val known = statuses.stream()
            .map(status -> "SLOW".equals(status) ? Status.UNKNOWN.getCode() : status)
            .collect(Collectors.toSet());
        return order.stream().filter(known::contains).findFirst().orElseGet(Status.UNKNOWN::getCode);
    }

    private static String healthCheckName(final String beanName) {
        return Strings.CI.removeEnd(Strings.CI.removeEnd(beanName, "HealthIndicator"), "HealthContributor");
    }

    /**
     * Run one health check on its own thread, timing it there, so that its time is not stretched by the checks read
     * before it. A check that has not answered yet is reused rather than started again, so one that hangs does not
     * pile up threads.
     *
     * @param name   the name of the check
     * @param status reads the status of the check
     * @return the running check
     */
    private HealthCheck healthCheck(final String name, final Supplier<String> status) {
        return healthChecks.compute(name, (_, previous) -> {
            if (previous != null && !previous.result().isDone()) {
                return previous;
            }
            val started = System.nanoTime();
            return new HealthCheck(started, CompletableFuture.supplyAsync(
                () -> new HealthResult(status.get(), TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started)), executor));
        });
    }

    /**
     * The order of health statuses, worst first, as the health endpoint is told to use it, such as CAS's own
     * {@code WARN} ahead of {@code DOWN}; Spring Boot's order when none is given.
     *
     * @return the status codes, worst first
     */
    private List<String> healthStatusOrder() {
        val order = Binder.get(applicationContext.getEnvironment())
            .bind("management.endpoint.health.status.order", Bindable.listOf(String.class))
            .orElseGet(List::of)
            .stream()
            .map(String::trim)
            .filter(StringUtils::isNotBlank)
            .map(code -> code.toUpperCase(Locale.ENGLISH))
            .toList();
        return order.isEmpty() ? DEFAULT_HEALTH_STATUS_ORDER : order;
    }

    /**
     * The status of a health check made of several checks, such as one per LDAP server being monitored: the worst
     * status of the checks in it, in the order the health endpoint uses. A status that order does not name counts as
     * the least severe.
     *
     * @param contributor the health check made of several checks
     * @param order       the status codes, worst first
     * @return the status
     */
    private static String statusOf(final CompositeHealthContributor contributor, final List<String> order) {
        val statuses = contributor.stream()
            .map(entry -> statusOf(entry.contributor(), order))
            .collect(Collectors.toSet());
        return order.stream().filter(statuses::contains).findFirst()
            .orElseGet(() -> statuses.stream().sorted().findFirst().orElseGet(Status.UNKNOWN::getCode));
    }

    private static String statusOf(final HealthContributor contributor, final List<String> order) {
        if (contributor instanceof final HealthIndicator indicator) {
            return indicator.health().getStatus().getCode();
        }
        if (contributor instanceof final CompositeHealthContributor composite) {
            return statusOf(composite, order);
        }
        return Status.UNKNOWN.getCode();
    }

    private static Map<String, Object> resources() {
        val memory = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        val resources = new LinkedHashMap<String, Object>();
        resources.put("heapUsed", memory.getUsed());
        resources.put("heapMax", memory.getMax());
        resources.put("threads", ManagementFactory.getThreadMXBean().getThreadCount());
        resources.put("processors", Runtime.getRuntime().availableProcessors());
        val system = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        if (system != null) {
            val cpu = system.getProcessCpuLoad();
            if (cpu >= 0) {
                resources.put("cpu", Math.min(1.0, cpu));
            }
        }
        return resources;
    }

    private Map<String, Object> tickets() {
        val registry = ticketRegistry.getIfAvailable();
        if (registry == null) {
            return Map.of("unavailable", true);
        }
        val tickets = new LinkedHashMap<String, Object>();
        tickets.put("sessions", registry.sessionCount());
        return tickets;
    }

    private Map<String, Object> applications() {
        val manager = servicesManager.getIfAvailable();
        if (manager == null) {
            return Map.of("unavailable", true);
        }
        val services = manager.getAllServices().stream().filter(Predicate.not(RegisteredService::isInternal)).toList();
        val byType = new TreeMap<String, Long>();
        services.forEach(service -> byType.merge(service.getFriendlyName(), 1L, Long::sum));
        val now = ZonedDateTime.now(ZoneOffset.UTC);
        val expiring = services.stream()
            .map(service -> new AbstractMap.SimpleImmutableEntry<>(service, expirationOf(service)))
            .filter(entry -> entry.getValue() != null && entry.getValue().isAfter(now)
                && entry.getValue().isBefore(now.plusDays(EXPIRY_WARNING_DAYS)))
            .sorted(Map.Entry.comparingByValue())
            .limit(5)
            .map(entry -> Map.<String, Object>of(
                "id", entry.getKey().getId(),
                "name", StringUtils.defaultString(entry.getKey().getName()),
                "expires", entry.getValue().toLocalDate().toString()))
            .toList();
        val applications = new LinkedHashMap<String, Object>();
        applications.put("total", services.size());
        applications.put("byType", byType);
        applications.put("expiring", expiring);
        return applications;
    }

    private static @Nullable ZonedDateTime expirationOf(final RegisteredService service) {
        val policy = service.getExpirationPolicy();
        if (policy == null || StringUtils.isBlank(policy.getExpirationDate())) {
            return null;
        }
        return DateTimeUtils.zonedDateTimeOf(policy.getExpirationDate());
    }

    /**
     * Remember an application that was saved, by whom and when.
     *
     * @param event the event
     */
    @EventListener
    public void onApplicationSaved(final CasRegisteredServiceSavedEvent event) {
        remember("saved", event.getRegisteredService());
    }

    /**
     * Remember an application that was deleted, by whom and when.
     *
     * @param event the event
     */
    @EventListener
    public void onApplicationDeleted(final CasRegisteredServiceDeletedEvent event) {
        remember("deleted", event.getRegisteredService());
    }

    /**
     * Remember that every application was deleted, by whom and when.
     *
     * @param event the event
     */
    @EventListener
    public void onApplicationsDeleted(final CasRegisteredServicesDeletedEvent event) {
        record(new PalantirApplicationChanges.Change(StringUtils.EMPTY, "All applications", StringUtils.EMPTY, "deleted",
            currentAuthor(), Instant.now()));
    }

    private void remember(final String action, @Nullable final RegisteredService service) {
        if (service == null || service.isInternal()) {
            return;
        }
        record(new PalantirApplicationChanges.Change(String.valueOf(service.getId()),
            StringUtils.defaultIfBlank(service.getName(), service.getServiceId()), service.getFriendlyName(), action,
            currentAuthor(), Instant.now()));
    }

    /**
     * Add a change to the front of the list. A change to the same application with the same action within a few
     * seconds is the same change seen twice, such as a save and the registry watcher that notices the saved file, and is
     * kept once with the author of whichever notice named one. The list is replaced as a whole, so readers always see
     * a complete list without locking.
     *
     * @param change the change
     */
    private void record(final PalantirApplicationChanges.Change change) {
        changes.updateAndGet(current -> {
            val latest = current.isEmpty() ? null : current.getFirst();
            if (latest != null && latest.id().equals(change.id()) && latest.action().equals(change.action())
                && latest.when().plus(DUPLICATE_CHANGE_WINDOW).isAfter(change.when())) {
                if (!SYSTEM_AUTHOR.equals(latest.author()) || SYSTEM_AUTHOR.equals(change.author())) {
                    return current;
                }
                val merged = new ArrayList<>(current);
                merged.set(0, new PalantirApplicationChanges.Change(latest.id(), latest.name(), latest.type(), latest.action(),
                    change.author(), latest.when()));
                return List.copyOf(merged);
            }
            return Stream.concat(Stream.of(change), current.stream()).limit(RECENT_CHANGES).toList();
        });
        invalidated.put("changes", Instant.now());
    }

    private static String currentAuthor() {
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
            || authentication instanceof AnonymousAuthenticationToken || StringUtils.isBlank(authentication.getName())) {
            return SYSTEM_AUTHOR;
        }
        return authentication.getName();
    }

    /**
     * The latest changes to applications: from the shared history when there is one, so every server's changes are
     * listed, otherwise those this server has seen. Changes to applications CAS keeps for itself are left out, and a
     * change is named by the application's current type when the application still exists.
     *
     * @return the changes and where they come from
     */
    private Map<String, Object> changes() {
        val manager = servicesManager.getIfAvailable();
        val shared = Optional.ofNullable(sharedChanges.getIfAvailable()).flatMap(source -> {
            try {
                return source.recent(RECENT_CHANGES << 1);
            } catch (final Exception e) {
                LOGGER.debug("Recent changes could not be read from the shared history: [{}]", e.getMessage());
                return Optional.empty();
            }
        });
        val recent = shared.orElseGet(changes::get)
            .stream()
            .map(change -> describe(change, manager))
            .flatMap(Optional::stream)
            .limit(RECENT_CHANGES)
            .toList();
        val result = new LinkedHashMap<String, Object>();
        result.put("scope", shared.isPresent() ? "cluster" : "node");
        result.put("recent", recent);
        result.put("since", Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime()).toString());
        return result;
    }

    /**
     * The type of an application that no longer exists, named as its definition would name it: the shared history
     * records the class, such as {@code SamlRegisteredService}.
     *
     * @param type the recorded type
     * @return the name of the type
     */
    private static String friendlyType(@Nullable final String type) {
        val name = StringUtils.defaultString(type);
        return switch (name) {
            case "CasRegisteredService" -> "CAS Client";
            case "SamlRegisteredService" -> "SAML2 Service Provider";
            case "OidcRegisteredService" -> "OpenID Connect Relying Party";
            case "OAuthRegisteredService" -> "OAuth2 Client";
            case "WSFederationRegisteredService" -> "WS Federation Relying Party";
            default -> Strings.CS.removeEnd(name, "RegisteredService");
        };
    }

    private static Optional<Map<String, Object>> describe(final PalantirApplicationChanges.Change change,
                                                          @Nullable final ServicesManager manager) {
        val id = NumberUtils.toLong(change.id(), -1L);
        val current = manager != null && id >= 0 ? manager.findServiceBy(id) : null;
        if (current != null && current.isInternal()) {
            return Optional.empty();
        }
        val entry = new LinkedHashMap<String, Object>();
        entry.put("when", change.when().toString());
        entry.put("action", change.action());
        entry.put("id", change.id());
        entry.put("name", StringUtils.defaultIfBlank(change.name(), current != null ? current.getName() : change.id()));
        entry.put("type", current != null ? current.getFriendlyName() : friendlyType(change.type()));
        entry.put("author", change.author());
        entry.put("exists", current != null);
        return Optional.of(entry);
    }

    /**
     * HTTP requests and runtime metrics from the meter registry: requests answered, their total and longest time and
     * how many failed, garbage collection pauses, errors logged and the system load.
     *
     * @return the metrics this server records
     */
    private Map<String, Object> traffic() {
        val registry = meterRegistry.getIfAvailable();
        if (registry == null) {
            return Map.of("unavailable", true);
        }
        val traffic = new LinkedHashMap<String, Object>();
        val requests = registry.find("http.server.requests").timers();
        if (!requests.isEmpty()) {
            val http = new LinkedHashMap<String, Object>();
            http.put("count", requests.stream().mapToLong(Timer::count).sum());
            http.put("totalSeconds", requests.stream().mapToDouble(timer -> timer.totalTime(TimeUnit.SECONDS)).sum());
            http.put("maxSeconds", requests.stream().mapToDouble(timer -> timer.max(TimeUnit.SECONDS)).max().orElse(0));
            http.put("serverErrors", countWhere(requests, "outcome", "SERVER_ERROR"));
            http.put("clientErrors", countWhere(requests, "outcome", "CLIENT_ERROR"));
            traffic.put("http", http);
        }
        val pauses = registry.find("jvm.gc.pause").timers();
        if (!pauses.isEmpty()) {
            val gc = new LinkedHashMap<String, Object>();
            gc.put("count", pauses.stream().mapToLong(Timer::count).sum());
            gc.put("totalSeconds", pauses.stream().mapToDouble(timer -> timer.totalTime(TimeUnit.SECONDS)).sum());
            gc.put("maxSeconds", pauses.stream().mapToDouble(timer -> timer.max(TimeUnit.SECONDS)).max().orElse(0));
            traffic.put("gc", gc);
        }
        loggedEvents(registry, "error").ifPresent(count -> traffic.put("errorsLogged", count));
        loggedEvents(registry, "warn").ifPresent(count -> traffic.put("warningsLogged", count));
        gauge(registry, "system.load.average.1m").ifPresent(value -> traffic.put("loadAverage", value));
        gauge(registry, "jvm.gc.overhead").ifPresent(value -> traffic.put("gcOverhead", value));
        gauge(registry, "jvm.gc.live.data.size")
            .ifPresent(used -> traffic.put("liveData", usage(used, gauge(registry, "jvm.gc.max.data.size").orElse(null))));
        gauge(registry, "process.files.open")
            .ifPresent(open -> traffic.put("files", usage(open, gauge(registry, "process.files.max").orElse(null))));
        return traffic;
    }

    /**
     * How many events of a level the logging framework logged since CAS started.
     *
     * @param registry the meter registry
     * @param level    the level, such as error
     * @return the count, when the logging framework records it
     */
    private static Optional<Double> loggedEvents(final MeterRegistry registry, final String level) {
        return Stream.of("log4j2.events", "logback.events")
            .map(name -> registry.find(name).tag("level", level).counters())
            .filter(Predicate.not(Collection::isEmpty))
            .findFirst()
            .map(counters -> counters.stream().mapToDouble(Counter::count).sum());
    }

    private static Optional<Double> gauge(final MeterRegistry registry, final String name) {
        return Optional.ofNullable(registry.find(name).gauge())
            .map(Gauge::value)
            .filter(value -> Double.isFinite(value) && value >= 0);
    }

    private static Map<String, Object> usage(final double used, final @Nullable Double max) {
        val usage = new LinkedHashMap<String, Object>();
        usage.put("used", used);
        if (max != null && max > 0) {
            usage.put("max", max);
        }
        return usage;
    }

    /**
     * The URLs of this server that answer slowest, from the HTTP request timers it already keeps: requests, average and
     * longest time per URL, the slowest by average first. Palantir's own requests, actuator endpoints and static files,
     * including those only matched by a wildcard pattern, are left out, so the list shows what users and applications
     * wait for.
     *
     * @return the slowest URLs
     */
    private Map<String, Object> endpoints() {
        val registry = meterRegistry.getIfAvailable();
        if (registry == null) {
            return Map.of("unavailable", true);
        }
        val byUri = registry.find("http.server.requests").timers()
            .stream()
            .filter(timer -> timer.count() > 0)
            .filter(timer -> {
                val uri = timer.getId().getTag("uri");
                return uri != null && uri.startsWith("/") && !uri.contains("**")
                    && ENDPOINTS_LEFT_OUT.stream().noneMatch(uri::startsWith);
            })
            .collect(Collectors.groupingBy(timer -> Objects.requireNonNull(timer.getId().getTag("uri"))));
        val slowest = byUri.entrySet()
            .stream()
            .map(entry -> {
                val timers = entry.getValue();
                val count = timers.stream().mapToLong(Timer::count).sum();
                val totalSeconds = timers.stream().mapToDouble(timer -> timer.totalTime(TimeUnit.SECONDS)).sum();
                val maxSeconds = timers.stream().mapToDouble(timer -> timer.max(TimeUnit.SECONDS)).max().orElse(0);
                return new SlowEndpoint(entry.getKey(), count, totalSeconds / count, maxSeconds,
                    countWhere(timers, "outcome", "SERVER_ERROR"));
            })
            .sorted(Comparator.comparingDouble(SlowEndpoint::meanSeconds).reversed())
            .limit(SLOWEST_ENDPOINTS)
            .toList();
        return Map.of("slowest", slowest, "measured", byUri.size());
    }

    private static long countWhere(final Collection<Timer> timers, final String tag, final String value) {
        return timers.stream()
            .filter(timer -> value.equals(timer.getId().getTag(tag)))
            .mapToLong(Timer::count)
            .sum();
    }

    /**
     * How one URL of this server answers.
     *
     * @param uri         the URL, as the request metrics name it
     * @param count       how many requests it answered
     * @param meanSeconds the average time it took
     * @param maxSeconds  the longest time it took recently
     * @param errors      how many requests ended with a server error
     */
    public record SlowEndpoint(String uri, long count, double meanSeconds, double maxSeconds, long errors) {
    }

    private record Section(String name, Set<String> endpoints, boolean administratorsOnly, Duration lifetime,
        Supplier<Map<String, Object>> supplier) {
    }

    private record CachedSection(Map<String, Object> value, Instant computed, Instant started) {
    }

    private record RunningSection(Instant started, CompletableFuture<Map<String, Object>> result) {
    }

    private record HealthCheck(long started, CompletableFuture<HealthResult> result) {
    }

    private record HealthResult(String status, long millis) {
    }
}
