package org.apereo.cas.monitor;

import module java.base;
import org.apereo.cas.ticket.registry.TicketRegistry;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;

/**
 * Monitors the status of a {@link TicketRegistry}
 * for exposing internal
 * state information used in status reports.
 * <p>
 * Counting sessions and service tickets can take a while in a registry that holds many tickets, so the counts are kept
 * for a few seconds and health checks in quick succession share them. A count the registry cannot make, reported as a
 * negative number, makes the status unknown.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
@RequiredArgsConstructor
public class TicketRegistryHealthIndicator extends AbstractHealthIndicator {
    private static final Duration COUNTS_LIFETIME = Duration.ofSeconds(15);

    /**
     * Ticket registry instance that exposes state info.
     */
    private final ObjectProvider<TicketRegistry> ticketRegistryProvider;

    /**
     * Threshold above which warnings are issued for service ticket count.
     */
    private final int serviceTicketCountWarnThreshold;

    /**
     * Threshold above which warnings are issued for session count.
     */
    private final int sessionCountWarnThreshold;

    private final AtomicReference<TicketCounts> counts = new AtomicReference<>();

    private static void buildHealthCheckStatus(final Health.Builder builder,
                                               final long sessionCount, final long ticketCount, final String msg) {
        builder
            .withDetail("sessionCount", sessionCount)
            .withDetail("ticketCount", ticketCount)
            .withDetail("message", msg);
    }

    @Override
    protected void doHealthCheck(final Health.Builder builder) {
        builder.withDetail("name", getClass().getSimpleName());

        ticketRegistryProvider.ifAvailable(registryState -> {
            val current = currentCounts(registryState);
            val sessionCount = current.sessions();
            val ticketCount = current.serviceTickets();

            if (sessionCount < 0 || ticketCount < 0) {
                val msg = String.format("Ticket registry %s reports unknown session and/or ticket counts.",
                    this.ticketRegistryProvider.getClass().getName());
                buildHealthCheckStatus(builder.unknown(), sessionCount, ticketCount, msg);
                return;
            }

            if (this.sessionCountWarnThreshold > -1 && sessionCount > this.sessionCountWarnThreshold) {
                val msg = String.format("Session count (%s) is above threshold %s. ", sessionCount, this.sessionCountWarnThreshold);
                buildHealthCheckStatus(builder.status("WARN"), sessionCount, ticketCount, msg);
                return;
            }

            if (this.serviceTicketCountWarnThreshold > -1 && ticketCount > this.serviceTicketCountWarnThreshold) {
                val msg = String.format("Service ticket count (%s) is above threshold %s.", ticketCount, this.serviceTicketCountWarnThreshold);
                buildHealthCheckStatus(builder.status("WARN"), sessionCount, ticketCount, msg);
                return;
            }

            buildHealthCheckStatus(builder.up(), sessionCount, ticketCount, "OK");
        });
    }

    private TicketCounts currentCounts(final TicketRegistry registry) {
        val cached = counts.get();
        if (cached != null && cached.counted().plus(COUNTS_LIFETIME).isAfter(Instant.now())) {
            return cached;
        }
        val fresh = new TicketCounts(registry.sessionCount(), registry.serviceTicketCount(), Instant.now());
        counts.set(fresh);
        return fresh;
    }

    private record TicketCounts(long sessions, long serviceTickets, Instant counted) {
    }
}
