package org.apereo.cas.web.report;

import org.apache.commons.codec.binary.StringUtils;
import org.apereo.cas.monitor.HealthCheckMonitor;
import org.apereo.cas.monitor.HealthStatus;
import org.apereo.cas.monitor.Monitor;
import org.apereo.cas.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.WebAsyncTask;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Reports overall CAS health based on the observations of the configured {@link HealthCheckMonitor} instance.
 *
 * @author Marvin S. Addison
 * @since 3.5
 */
@Controller("healthCheckController")
@RequestMapping("/status")
public class HealthCheckController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckController.class);
    
    private final Monitor<HealthStatus> healthCheckMonitor;
    private final long timeout;

    public HealthCheckController(final Monitor<HealthStatus> healthCheckMonitor, final long timeout) {
        this.healthCheckMonitor = healthCheckMonitor;
        this.timeout = timeout;
    }

    /**
     * Handle request.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @GetMapping
    @ResponseBody
    protected WebAsyncTask<HealthStatus> handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        final Callable<HealthStatus> asyncTask = () -> {
            final HealthStatus healthStatus = healthCheckMonitor.observe();
            response.setStatus(healthStatus.getCode().value());
            
            if (StringUtils.equals(request.getParameter("format"), "json")) {
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                JsonUtils.render(healthStatus.getDetails(), response);
            } else {
                final StringBuilder sb = new StringBuilder();
                sb.append("Health: ").append(healthStatus.getCode());

                final AtomicInteger i = new AtomicInteger();
                healthStatus.getDetails().forEach((name, status) -> {
                    response.addHeader("X-CAS-" + name, String.format("%s;%s", status.getCode(), status.getDescription()));
                    sb.append("\n\n\t").append(i.incrementAndGet()).append('.').append(name).append(": ");
                    sb.append(status.getCode());
                    if (status.getDescription() != null) {
                        sb.append(" - ").append(status.getDescription());
                    }
                });
                response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                response.getOutputStream().write(sb.toString().getBytes(response.getCharacterEncoding()));
            }
            return null;
        };

        return new WebAsyncTask<>(timeout, asyncTask);
    }
}
