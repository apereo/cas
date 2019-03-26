package org.apereo.cas.web.report;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Scott Battaglia
 * @since 3.3.5
 */
@Endpoint(id = "statistics", enableByDefault = false)
public class StatisticsEndpoint extends BaseCasActuatorEndpoint {
    private final ZonedDateTime upTimeStartDate = ZonedDateTime.now(ZoneOffset.UTC);

    private final CentralAuthenticationService centralAuthenticationService;

    public StatisticsEndpoint(final CentralAuthenticationService centralAuthenticationService,
                              final CasConfigurationProperties casProperties) {
        super(casProperties);
        this.centralAuthenticationService = centralAuthenticationService;
    }

    /**
     * Gets availability times of the server.
     *
     * @return the availability
     */
    @ReadOperation
    public Map<String, Object> handle() {
        val model = new HashMap<String, Object>();

        val diff = Duration.between(this.upTimeStartDate, ZonedDateTime.now(ZoneOffset.UTC));
        model.put("upTime", diff.getSeconds());

        val runtime = Runtime.getRuntime();
        model.put("totalMemory", FileUtils.byteCountToDisplaySize(runtime.totalMemory()));
        model.put("maxMemory", FileUtils.byteCountToDisplaySize(runtime.maxMemory()));
        model.put("freeMemory", FileUtils.byteCountToDisplaySize(runtime.freeMemory()));

        val unexpiredTgts = new AtomicInteger();
        val unexpiredSts = new AtomicInteger();
        val expiredTgts = new AtomicInteger();
        val expiredSts = new AtomicInteger();

        val tickets = this.centralAuthenticationService.getTickets(ticket -> true);
        tickets.forEach(ticket -> {
            if (ticket instanceof ServiceTicket) {
                if (ticket.isExpired()) {
                    this.centralAuthenticationService.deleteTicket(ticket.getId());
                    expiredSts.incrementAndGet();
                } else {
                    unexpiredSts.incrementAndGet();
                }
            } else {
                if (ticket.isExpired()) {
                    this.centralAuthenticationService.deleteTicket(ticket.getId());
                    expiredTgts.incrementAndGet();
                } else {
                    unexpiredTgts.incrementAndGet();
                }
            }
        });

        model.put("unexpiredTgts", unexpiredTgts);
        model.put("unexpiredSts", unexpiredSts);
        model.put("expiredTgts", expiredTgts);
        model.put("expiredSts", expiredSts);

        return model;
    }
}
