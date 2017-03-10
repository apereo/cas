package org.apereo.cas.web.report;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Scott Battaglia
 * @since 3.3.5
 */
public class StatisticsController extends BaseCasMvcEndpoint implements ServletContextAware {

    private static final int NUMBER_OF_BYTES_IN_A_KILOBYTE = 1024;
    private static final String MONITORING_VIEW_STATISTICS = "monitoring/viewStatistics";

    private final ZonedDateTime upTimeStartDate = ZonedDateTime.now(ZoneOffset.UTC);

    private final CentralAuthenticationService centralAuthenticationService;
    private final MetricRegistry metricsRegistry;
    private final HealthCheckRegistry healthCheckRegistry;
    private final CasConfigurationProperties casProperties;

    public StatisticsController(final CentralAuthenticationService centralAuthenticationService,
                                final MetricRegistry metricsRegistry,
                                final HealthCheckRegistry healthCheckRegistry,
                                final CasConfigurationProperties casProperties) {
        super("casstats", "/stats", casProperties.getMonitor().getEndpoints().getStatistics(), casProperties);
        this.centralAuthenticationService = centralAuthenticationService;
        this.metricsRegistry = metricsRegistry;
        this.healthCheckRegistry = healthCheckRegistry;
        this.casProperties = casProperties; 
    }

    /**
     * Gets availability times of the server.
     *
     * @param request  the http servlet request
     * @param response the http servlet response
     * @return the availability
     */
    @GetMapping(value = "/getAvailability")
    @ResponseBody
    public Map<String, Object> getAvailability(final HttpServletRequest request,
                                               final HttpServletResponse response) {
        ensureEndpointAccessIsAuthorized(request, response);

        final Map<String, Object> model = new HashMap<>();
        final Duration diff = Duration.between(this.upTimeStartDate, ZonedDateTime.now(ZoneOffset.UTC));
        model.put("upTime", diff.getSeconds());
        return model;
    }

    /**
     * Gets memory stats.
     *
     * @param request  the http servlet request
     * @param response the http servlet response
     * @return the memory stats
     */
    @GetMapping(value = "/getMemStats")
    @ResponseBody
    public Map<String, Object> getMemoryStats(final HttpServletRequest request,
                                              final HttpServletResponse response) {
        ensureEndpointAccessIsAuthorized(request, response);

        final Map<String, Object> model = new HashMap<>();
        model.put("totalMemory", convertToMegaBytes(Runtime.getRuntime().totalMemory()));
        model.put("maxMemory", convertToMegaBytes(Runtime.getRuntime().maxMemory()));
        model.put("freeMemory", convertToMegaBytes(Runtime.getRuntime().freeMemory()));
        return model;
    }

    /**
     * Gets ticket stats.
     *
     * @param request  the http servlet request
     * @param response the http servlet response
     * @return the ticket stats
     */
    @GetMapping(value = "/getTicketStats")
    @ResponseBody
    public Map<String, Object> getTicketStats(final HttpServletRequest request,
                                              final HttpServletResponse response) {
        ensureEndpointAccessIsAuthorized(request, response);

        final Map<String, Object> model = new HashMap<>();

        int unexpiredTgts = 0;
        int unexpiredSts = 0;
        int expiredTgts = 0;
        int expiredSts = 0;

        final Collection<Ticket> tickets = this.centralAuthenticationService.getTickets(ticket -> true);

        for (final Ticket ticket : tickets) {
            if (ticket instanceof ServiceTicket) {
                if (ticket.isExpired()) {
                    expiredSts++;
                } else {
                    unexpiredSts++;
                }
            } else {
                if (ticket.isExpired()) {
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

    /**
     * Handles the request.
     *
     * @param httpServletRequest  the http servlet request
     * @param httpServletResponse the http servlet response
     * @return the model and view
     * @throws Exception the exception
     */
    @GetMapping
    protected ModelAndView handleRequestInternal(final HttpServletRequest httpServletRequest,
                                                 final HttpServletResponse httpServletResponse) throws Exception {
        ensureEndpointAccessIsAuthorized(httpServletRequest, httpServletResponse);
        
        final ModelAndView modelAndView = new ModelAndView(MONITORING_VIEW_STATISTICS);
        modelAndView.addObject("pageTitle", modelAndView.getViewName());
        modelAndView.addObject("availableProcessors", Runtime.getRuntime().availableProcessors());
        modelAndView.addObject("casTicketSuffix", casProperties.getHost().getName());
        modelAndView.getModel().putAll(getAvailability(httpServletRequest, httpServletResponse));
        modelAndView.addObject("startTime", this.upTimeStartDate.toLocalDateTime());

        modelAndView.getModel().putAll(getMemoryStats(httpServletRequest, httpServletResponse));
        return modelAndView;
    }

    /**
     * Convert to megabytes from bytes.
     *
     * @param bytes the total number of bytes
     * @return value converted to MB
     */
    private static double convertToMegaBytes(final double bytes) {
        return bytes / NUMBER_OF_BYTES_IN_A_KILOBYTE / NUMBER_OF_BYTES_IN_A_KILOBYTE;
    }

    @Override
    public void setServletContext(final ServletContext servletContext) {
        servletContext.setAttribute(MetricsServlet.METRICS_REGISTRY, this.metricsRegistry);
        servletContext.setAttribute(MetricsServlet.SHOW_SAMPLES, Boolean.TRUE);
        servletContext.setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY, this.healthCheckRegistry);
    }
}
