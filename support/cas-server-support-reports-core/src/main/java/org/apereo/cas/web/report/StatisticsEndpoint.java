package org.apereo.cas.web.report;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import io.swagger.v3.oas.annotations.Operation;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Statistics endpoint reports back on cas metrics and ticket stats.
 *
 * @author Scott Battaglia
 * @since 3.3.5
 */
@Endpoint(id = "statistics", enableByDefault = false)
public class StatisticsEndpoint extends BaseCasActuatorEndpoint {
    private final ZonedDateTime upTimeStartDate = ZonedDateTime.now(ZoneOffset.UTC);

    private final ObjectProvider<TicketRegistry> ticketRegistry;

    public StatisticsEndpoint(final ObjectProvider<TicketRegistry> ticketRegistry,
                              final CasConfigurationProperties casProperties) {
        super(casProperties);
        this.ticketRegistry = ticketRegistry;
    }

    /**
     * Gets availability times of the server.
     *
     * @return the availability
     */
    @ReadOperation
    @Operation(summary = "Get a report of CAS statistics on tickets. Expired tickets will be removed")
    public Map<String, Object> handle() {
        val model = new HashMap<String, Object>();

        val diff = Duration.between(upTimeStartDate, ZonedDateTime.now(ZoneOffset.UTC));
        model.put("upTime", diff.getSeconds());

        val runtime = Runtime.getRuntime();
        model.put("totalMemory", FileUtils.byteCountToDisplaySize(runtime.totalMemory()));
        model.put("maxMemory", FileUtils.byteCountToDisplaySize(runtime.maxMemory()));
        model.put("freeMemory", FileUtils.byteCountToDisplaySize(runtime.freeMemory()));

        val validTickets = new AtomicInteger();
        val expiredTickets = new AtomicInteger();
        try (val stream = ticketRegistry.getObject().stream()) {
            stream.forEach(Unchecked.consumer(ticket -> {
                if (ticket.isExpired()) {
                    ticketRegistry.getObject().deleteTicket(ticket.getId());
                    expiredTickets.incrementAndGet();
                } else {
                    validTickets.incrementAndGet();
                }
            }));
        }
        
        model.put("expiredTickets", expiredTickets);
        model.put("validTickets", validTickets);
        return model;
    }
}
