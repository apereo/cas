package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.BaseCasActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

/**
 * Statistics endpoint reports back on cas metrics and ticket stats.
 * <p>
 * Tickets are counted per type in one pass, the way the ticket registry counts them, so the answer stays quick however
 * many tickets the registry holds and the total, sessions and service tickets add up. Every kind of ticket is counted,
 * whichever module issues it, and named after its definition in the ticket catalog; a kind the catalog does not know
 * is named by its prefix. Expired tickets are left to the registry cleaner, and the counts are kept for a few seconds
 * so that callers in quick succession, such as several dashboards open at once, share one count.
 *
 * @author Scott Battaglia
 * @since 3.3.5
 */
@Slf4j
@Endpoint(id = "statistics", defaultAccess = Access.NONE)
public class StatisticsEndpoint extends BaseCasActuatorEndpoint {
    private static final Duration COUNTS_LIFETIME = Duration.ofSeconds(15);

    private static final Pattern WORD_BOUNDARY = Pattern.compile("(?<=[a-z0-9])(?=[A-Z])");

    private final ZonedDateTime upTimeStartDate = ZonedDateTime.now(ZoneOffset.UTC);

    private final ObjectProvider<TicketRegistry> ticketRegistry;

    private final ObjectProvider<TicketCatalog> ticketCatalog;

    private final AtomicReference<TicketCounts> counts = new AtomicReference<>();

    public StatisticsEndpoint(final ObjectProvider<TicketRegistry> ticketRegistry,
                              final ObjectProvider<TicketCatalog> ticketCatalog,
                              final CasConfigurationProperties casProperties) {
        super(casProperties);
        this.ticketRegistry = ticketRegistry;
        this.ticketCatalog = ticketCatalog;
    }

    /**
     * Gets availability times of the server, its memory and the number of tickets in the ticket registry: in all, of
     * each type, and how many are sessions and service tickets. Counts the registry cannot make are reported as
     * negative numbers, with no types.
     *
     * @return the availability
     */
    @ReadOperation
    @Operation(summary = "Get a report of CAS statistics on uptime, memory and the tickets in the ticket registry")
    public Map<String, Object> statistics() {
        val model = new LinkedHashMap<String, Object>();

        val diff = Duration.between(upTimeStartDate, ZonedDateTime.now(ZoneOffset.UTC));
        model.put("upTime", diff.toSeconds());

        val runtime = Runtime.getRuntime();
        model.put("totalMemory", FileUtils.byteCountToDisplaySize(runtime.totalMemory()));
        model.put("maxMemory", FileUtils.byteCountToDisplaySize(runtime.maxMemory()));
        model.put("freeMemory", FileUtils.byteCountToDisplaySize(runtime.freeMemory()));

        val current = currentCounts();
        model.put("tickets", current.tickets());
        model.put("sessions", current.sessions());
        model.put("serviceTickets", current.serviceTickets());
        model.put("ticketsByType", current.types().stream().map(type -> Map.<String, Object>of(
            "prefix", type.prefix(), "name", type.name(), "kind", type.kind(), "count", type.count())).toList());
        model.put("counted", current.counted().toString());
        return model;
    }

    private TicketCounts currentCounts() {
        val cached = counts.get();
        if (cached != null && cached.counted().plus(COUNTS_LIFETIME).isAfter(Instant.now())) {
            return cached;
        }
        val fresh = countTickets();
        counts.set(fresh);
        return fresh;
    }

    private TicketCounts countTickets() {
        val registry = ticketRegistry.getIfAvailable();
        if (registry == null) {
            return TicketCounts.unknown();
        }
        try {
            val definitions = Optional.ofNullable(ticketCatalog.getIfAvailable())
                .map(TicketCatalog::findAll)
                .orElseGet(List::of)
                .stream()
                .collect(Collectors.toMap(TicketDefinition::getPrefix, Function.identity(), (first, second) -> first));
            val types = registry.countTicketsByType()
                .entrySet()
                .stream()
                .map(entry -> describe(entry.getKey(), entry.getValue(), definitions.get(entry.getKey())))
                .sorted(Comparator.comparingLong(TicketType::count).reversed().thenComparing(TicketType::name))
                .toList();
            return new TicketCounts(
                types.stream().mapToLong(TicketType::count).sum(),
                types.stream().filter(type -> TicketType.SESSION.equals(type.kind())).mapToLong(TicketType::count).sum(),
                types.stream().filter(type -> TicketType.SERVICE.equals(type.kind())).mapToLong(TicketType::count).sum(),
                types, Instant.now());
        } catch (final Exception e) {
            LOGGER.warn("Tickets in the ticket registry could not be counted: [{}]", e.getMessage());
            return TicketCounts.unknown();
        }
    }

    private static TicketType describe(final String prefix, final long count, @Nullable final TicketDefinition definition) {
        if (definition == null) {
            return new TicketType(prefix, prefix, TicketType.OTHER, count);
        }
        val implementation = definition.getImplementationClass();
        val kind = TicketGrantingTicket.class.isAssignableFrom(implementation) ? TicketType.SESSION
            : ServiceTicket.class.isAssignableFrom(implementation) ? TicketType.SERVICE : TicketType.OTHER;
        val name = String.join(" ", WORD_BOUNDARY.split(definition.getApiClass().getSimpleName()));
        return new TicketType(prefix, name, kind, count);
    }

    private record TicketCounts(long tickets, long sessions, long serviceTickets, List<TicketType> types, Instant counted) {
        static TicketCounts unknown() {
            return new TicketCounts(-1, -1, -1, List.of(), Instant.now());
        }
    }

    private record TicketType(String prefix, String name, String kind, long count) {
        static final String SESSION = "session";

        static final String SERVICE = "service";

        static final String OTHER = "other";
    }
}
