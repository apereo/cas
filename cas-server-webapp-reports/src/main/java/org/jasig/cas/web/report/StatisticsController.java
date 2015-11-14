package org.jasig.cas.web.report;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import org.apache.commons.collections4.functors.TruePredicate;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * @author Scott Battaglia
 * @since 3.3.5
 */
@Component
@Controller("statisticsController")
public final class StatisticsController implements ServletContextAware {

    private static final int NUMBER_OF_MILLISECONDS_IN_A_DAY = 86400000;

    private static final int NUMBER_OF_MILLISECONDS_IN_AN_HOUR = 3600000;

    private static final int NUMBER_OF_MILLISECONDS_IN_A_MINUTE = 60000;

    private static final int NUMBER_OF_MILLISECONDS_IN_A_SECOND = 1000;

    private static final int NUMBER_OF_BYTES_IN_A_KILOBYTE = 1024;

    private  static final String MONITORING_VIEW_STATISTICS = "monitoring/viewStatistics";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Date upTimeStartDate = new Date();

    @Value("${host.name:cas01.example.org}")
    private String casTicketSuffix;

    @Autowired
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("metrics")
    private MetricRegistry metricsRegistry;

    @Autowired
    @Qualifier("healthCheckMetrics")
    private HealthCheckRegistry healthCheckRegistry;


    /**
     * Gets memory stats.
     *
     * @param request  the request
     * @param response the response
     * @return the memory stats
     * @throws Exception the exception
     */
    @RequestMapping(value="/statistics/stats/getMemoryStats", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getMemoryStats(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("totalMemory", convertToMegaBytes(Runtime.getRuntime().totalMemory()));
        responseMap.put("maxMemory", convertToMegaBytes(Runtime.getRuntime().maxMemory()));
        responseMap.put("freeMemory", convertToMegaBytes(Runtime.getRuntime().freeMemory()));
        return responseMap;

    }

    /**
     * Gets lifetime.
     *
     * @param request  the request
     * @param response the response
     * @return the lifetime
     * @throws Exception the exception
     */
    @RequestMapping(value="/statistics/stats/getLifetime", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getLifetime(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("startTime", this.upTimeStartDate);
        final double difference = System.currentTimeMillis() - this.upTimeStartDate.getTime();

        responseMap.put("upTime", calculateUptime(difference, new LinkedList<Integer>(
                        Arrays.asList(NUMBER_OF_MILLISECONDS_IN_A_DAY, NUMBER_OF_MILLISECONDS_IN_AN_HOUR,
                                NUMBER_OF_MILLISECONDS_IN_A_MINUTE, NUMBER_OF_MILLISECONDS_IN_A_SECOND, 1)),
                new LinkedList<String>(Arrays.asList("day", "hour", "minute", "second", "millisecond"))));
        return responseMap;

    }

    /**
     * Gets runtime stats.
     *
     * @param request  the request
     * @param response the response
     * @return the runtime stats
     * @throws Exception the exception
     */
    @RequestMapping(value="/statistics/stats/getRuntimeStats", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getRuntimeStats(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        responseMap.put("serverHostName", request.getServerName());
        responseMap.put("serverIpAddress", request.getLocalAddr());
        responseMap.put("casTicketSuffix", this.casTicketSuffix);
        return responseMap;

    }

    /**
     * Gets ticket stats.
     *
     * @param request  the request
     * @param response the response
     * @return the ticket stats
     * @throws Exception the exception
     */
    @RequestMapping(value="/statistics/stats/getTicketStats", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getTicketStats(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final Map<String, Object> responseMap = new HashMap<>();
        int unexpiredTgts = 0;
        int unexpiredSts = 0;
        int expiredTgts = 0;
        int expiredSts = 0;

        try {
            final Collection<Ticket> tickets = this.centralAuthenticationService.getTickets(TruePredicate.INSTANCE);

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
        } catch (final UnsupportedOperationException e) {
            logger.debug("The ticket registry doesn't support this information.", e);
        }

        responseMap.put("unexpiredTgts", unexpiredTgts);
        responseMap.put("unexpiredSts", unexpiredSts);
        responseMap.put("expiredTgts", expiredTgts);
        responseMap.put("expiredSts", expiredSts);
        return responseMap;

    }


    /**
     * Handles the request.
     *
     * @param httpServletRequest the http servlet request
     * @param httpServletResponse the http servlet response
     * @return the model and view
     * @throws Exception the exception
     */
    @RequestMapping(value="/statistics", method = RequestMethod.GET)
    protected ModelAndView handleRequestInternal(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse)
                throws Exception {
        return new ModelAndView(MONITORING_VIEW_STATISTICS);
    }

    /**
     * Convert to megabytes from bytes.
     * @param bytes the total number of bytes
     * @return value converted to MB
     */
    private double convertToMegaBytes(final double bytes) {
        return bytes / NUMBER_OF_BYTES_IN_A_KILOBYTE / NUMBER_OF_BYTES_IN_A_KILOBYTE;
    }
    /**
     * Calculates the up time.
     *
     * @param difference the difference
     * @param calculations the calculations
     * @param labels the labels
     * @return the uptime as a string.
     */
    protected String calculateUptime(final double difference, final Queue<Integer> calculations, final Queue<String> labels) {
        if (calculations.isEmpty()) {
            return "";
        }

        final int value = calculations.remove();
        final double time = Math.floor(difference / value);
        final double newDifference = difference - time * value;
        final String currentLabel = labels.remove();
        final String label = time == 0 || time > 1 ? currentLabel + 's' : currentLabel;

        return Integer.toString((int) time) + ' ' + label + ' ' + calculateUptime(newDifference, calculations, labels);
    }

    @Override
    public void setServletContext(final ServletContext servletContext) {
        servletContext.setAttribute(MetricsServlet.METRICS_REGISTRY, this.metricsRegistry);
        servletContext.setAttribute(MetricsServlet.SHOW_SAMPLES, Boolean.TRUE);
        servletContext.setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY, this.healthCheckRegistry);
    }
}
