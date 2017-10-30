package org.apereo.cas.web.report;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.spi.DelegatingAuditTrailManager;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.web.BaseCasMvcEndpoint;
import org.apereo.inspektr.audit.AuditActionContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.request.async.WebAsyncTask;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author Scott Battaglia
 * @since 3.3.5
 */
public class StatisticsController extends BaseCasMvcEndpoint implements ServletContextAware {

    private static final int NUMBER_OF_BYTES_IN_A_KILOBYTE = 1024;
    private static final String MONITORING_VIEW_STATISTICS = "monitoring/viewStatistics";

    private final ZonedDateTime upTimeStartDate = ZonedDateTime.now(ZoneOffset.UTC);

    private final DelegatingAuditTrailManager auditTrailManager;
    private final CentralAuthenticationService centralAuthenticationService;
    private final MetricRegistry metricsRegistry;
    private final HealthCheckRegistry healthCheckRegistry;
    private final CasConfigurationProperties casProperties;

    public StatisticsController(final DelegatingAuditTrailManager auditTrailManager,
                                final CentralAuthenticationService centralAuthenticationService,
                                final MetricRegistry metricsRegistry,
                                final HealthCheckRegistry healthCheckRegistry,
                                final CasConfigurationProperties casProperties) {
        super("casstats", "/stats", casProperties.getMonitor().getEndpoints().getStatistics(), casProperties);
        this.auditTrailManager = auditTrailManager;
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
     * Gets authn audit.
     *
     * @param request  the request
     * @param response the response
     * @return the authn audit
     */
    @GetMapping(value = "/getAuthnAudit")
    @ResponseBody
    public Set<AuditActionContext> getAuthnAudit(final HttpServletRequest request,
                                                 final HttpServletResponse response) {
        ensureEndpointAccessIsAuthorized(request, response);
        return this.auditTrailManager.get();
    }

    /**
     * Gets authn audit summary.
     *
     * @param request  the request
     * @param response the response
     * @param start    the start
     * @param range    the range
     * @param scale    the scale
     * @return the authn audit
     */
    @GetMapping(value = "/getAuthnAudit/summary")
    @ResponseBody
    public WebAsyncTask<Collection<AuthenticationAuditSummary>> getAuthnAuditSummary(final HttpServletRequest request,
                                                                                     final HttpServletResponse response,
                                                                                     @RequestParam final long start,
                                                                                     @RequestParam final String range,
                                                                                     @RequestParam final String scale) {
        ensureEndpointAccessIsAuthorized(request, response);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        final Callable<Collection<AuthenticationAuditSummary>> asyncTask = () -> {
            final Set<AuditActionContext> audits = getAuthnAudit(request, response);
            final LocalDateTime startDate = DateTimeUtils.localDateTimeOf(start);
            final LocalDateTime endDate = startDate.plus(Duration.parse(range));

            final List<AuditActionContext> authnEvents = audits.stream()
                    .filter(a -> {
                        final LocalDateTime actionTime = DateTimeUtils.localDateTimeOf(a.getWhenActionWasPerformed());
                        return (actionTime.isEqual(startDate) || actionTime.isAfter(startDate))
                                && (actionTime.isEqual(endDate) || actionTime.isBefore(endDate))
                                && a.getActionPerformed().matches("AUTHENTICATION_(SUCCESS|FAILED)");
                    })
                    .sorted(Comparator.comparing(AuditActionContext::getWhenActionWasPerformed))
                    .collect(Collectors.toList());

            final Duration steps = Duration.parse(scale);
            final Map<Integer, LocalDateTime> buckets = new LinkedHashMap<>();

            LocalDateTime dt = startDate;
            Integer index = 0;
            while (dt != null) {
                buckets.put(index++, dt);
                dt = dt.plus(steps);
                if (dt.isAfter(endDate)) {
                    dt = null;
                }
            }

            final Map<LocalDateTime, AuthenticationAuditSummary> summary = new LinkedHashMap<>();
            boolean foundBucket = false;
            for (final AuditActionContext event : authnEvents) {
                foundBucket = false;
                for (int i = 0; i < buckets.keySet().size(); i++) {
                    final LocalDateTime actionTime = DateTimeUtils.localDateTimeOf(event.getWhenActionWasPerformed());
                    final LocalDateTime bucketDateTime = buckets.get(i);
                    if (actionTime.isEqual(bucketDateTime) || actionTime.isAfter(bucketDateTime)) {
                        for (int j = 0; j < buckets.keySet().size(); j++) {
                            final LocalDateTime nextBucketDateTime = buckets.get(j);
                            if (actionTime.isBefore(nextBucketDateTime)) {
                                final LocalDateTime bucketToUse = buckets.get(j - 1);
                                final AuthenticationAuditSummary values;
                                if (summary.containsKey(bucketToUse)) {
                                    values = summary.get(bucketToUse);
                                } else {
                                    final long l = bucketToUse.toInstant(ZoneOffset.UTC).toEpochMilli();
                                    values = new AuthenticationAuditSummary(l);
                                }
                                if (event.getActionPerformed().contains("SUCCESS")) {
                                    values.incrementSuccess();
                                } else {
                                    values.incrementFailure();
                                }
                                summary.put(bucketToUse, values);

                                foundBucket = true;
                                break;
                            }
                        }
                        if (foundBucket) {
                            break;
                        }
                    }
                }
            }
            final Collection<AuthenticationAuditSummary> values = summary.values();
            return values;
        };
        return new WebAsyncTask<>(casProperties.getHttpClient().getAsyncTimeout(), asyncTask);
    }

    private static class AuthenticationAuditSummary {
        private final long time;
        private long successes;
        private long failures;

        /**
         * Instantiates a new Authentication audit summary.
         *
         * @param time the time
         */
        AuthenticationAuditSummary(final long time) {
            this.time = time;
        }

        public long getTime() {
            return time;
        }

        public long getSuccesses() {
            return successes;
        }

        public long getFailures() {
            return failures;
        }

        public void incrementSuccess() {
            successes++;
        }

        public void incrementFailure() {
            failures++;
        }
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
    public Map<String, Object> getTicketStats(final HttpServletRequest request, final HttpServletResponse response) {
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

    /**
     * Handles the request.
     *
     * @param httpServletRequest  the http servlet request
     * @param httpServletResponse the http servlet response
     * @return the model and view
     */
    @GetMapping
    protected ModelAndView handleRequestInternal(final HttpServletRequest httpServletRequest,
                                                 final HttpServletResponse httpServletResponse) {
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
