package org.apereo.cas.web.report;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.web.BaseCasMvcEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Scott Battaglia
 * @since 3.3.5
 */
@Slf4j
@Endpoint(id = "statistics", enableByDefault = false)
public class StatisticsEndpoint extends BaseCasMvcEndpoint {
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
        final Map<String, Object> model = new HashMap<>();

        final var diff = Duration.between(this.upTimeStartDate, ZonedDateTime.now(ZoneOffset.UTC));
        model.put("upTime", diff.getSeconds());

        final var runtime = Runtime.getRuntime();
        model.put("totalMemory", FileUtils.byteCountToDisplaySize(runtime.totalMemory()));
        model.put("maxMemory", FileUtils.byteCountToDisplaySize(runtime.maxMemory()));
        model.put("freeMemory", FileUtils.byteCountToDisplaySize(runtime.freeMemory()));

        var unexpiredTgts = 0;
        var unexpiredSts = 0;
        var expiredTgts = 0;
        var expiredSts = 0;

        final var tickets = this.centralAuthenticationService.getTickets(ticket -> true);

        for (final var ticket : tickets) {
            if (ticket instanceof ServiceTicket) {
                if (ticket.isExpired()) {
                    this.centralAuthenticationService.deleteTicket(ticket.getId());
                    expiredSts++;
                } else {
                    unexpiredSts++;
                }
            } else {
                if (ticket.isExpired()) {
                    this.centralAuthenticationService.deleteTicket(ticket.getId());
                    expiredTgts++;
                } else {
                    unexpiredTgts++;
                }
            }
        }

        model.put("unexpiredTgts", unexpiredTgts);
        model.put("unexpiredSts", unexpiredSts);
        model.put("expiredTgts", expiredTgts);
        model.put("expiredSts", expiredSts);

        return model;
    }
}
