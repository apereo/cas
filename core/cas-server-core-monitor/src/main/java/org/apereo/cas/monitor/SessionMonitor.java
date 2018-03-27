package org.apereo.cas.monitor;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

/**
 * Monitors the status of a {@link TicketRegistry}
 * for exposing internal
 * state information used in status reports.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
@Slf4j
@AllArgsConstructor
public class SessionMonitor extends AbstractHealthIndicator {

    /**
     * Ticket registry instance that exposes state info.
     */
    private final TicketRegistry registryState;

    /**
     * Threshold above which warnings are issued for service ticket count.
     */
    private int serviceTicketCountWarnThreshold = -1;

    /**
     * Threshold above which warnings are issued for session count.
     */
    private int sessionCountWarnThreshold = -1;

    @Override
    protected void doHealthCheck(final Health.Builder builder) {

        final long sessionCount = this.registryState.sessionCount();
        final long ticketCount = this.registryState.serviceTicketCount();

        if (sessionCount == Integer.MIN_VALUE || ticketCount == Integer.MIN_VALUE) {
            final String msg = String.format("Ticket registry %s reports unknown session and/or ticket counts.", this.registryState.getClass().getName());
            buildHealthCheckStatus(builder.unknown(), sessionCount, ticketCount, msg);
            return;
        }

        if (this.sessionCountWarnThreshold > -1 && sessionCount > this.sessionCountWarnThreshold) {
            final String msg = String.format("Session count (%s) is above threshold %s. ", sessionCount, this.sessionCountWarnThreshold);
            buildHealthCheckStatus(builder.status("WARN"), sessionCount, ticketCount, msg);
            return;
        }

        if (this.serviceTicketCountWarnThreshold > -1 && ticketCount > this.serviceTicketCountWarnThreshold) {
            final String msg = String.format("Service ticket count (%s) is above threshold %s.", ticketCount, this.serviceTicketCountWarnThreshold);
            buildHealthCheckStatus(builder.status("WARN"), sessionCount, ticketCount, msg);
            return;
        }

        buildHealthCheckStatus(builder.up(), sessionCount, ticketCount, "OK");
    }

    private void buildHealthCheckStatus(final Health.Builder builder,
                                        final long sessionCount, final long ticketCount, final String msg) {
        builder
            .withDetail("sessionCount", sessionCount)
            .withDetail("ticketCount", ticketCount)
            .withDetail("message", msg);
    }
}
