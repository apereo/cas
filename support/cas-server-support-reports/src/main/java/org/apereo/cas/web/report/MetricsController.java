package org.apereo.cas.web.report;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasMvcEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.Properties;

/**
 * This is {@link MetricsController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class MetricsController extends BaseCasMvcEndpoint {

    private final Properties initParameters = new Properties();

    @Autowired
    @Qualifier("metrics")
    private MetricRegistry metrics;

    public MetricsController(final CasConfigurationProperties casProperties) {
        super("casmetrics", "/metrics", casProperties.getMonitor().getEndpoints().getMetrics(), casProperties);
    }

    /**
     * Handle.
     *
     * @param request  the request
     * @param response the response
     * @throws Exception the exception
     */
    @GetMapping
    public void handle(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        ensureEndpointAccessIsAuthorized(request, response);
        final MetricsServlet servlet = new MetricsServlet(this.metrics);
        servlet.init(new DelegatingServletConfig());
        servlet.service(request, response);
    }

    private ServletContext getServletContext() {
        if (applicationContext instanceof WebApplicationContext) {
            final WebApplicationContext webCtx = (WebApplicationContext) applicationContext;
            return webCtx.getServletContext();
        }
        throw new IllegalArgumentException("Cant determine the web application context");
    }

    private class DelegatingServletConfig implements ServletConfig {

        @Override
        public String getServletName() {
            return MetricsController.this.getClass().getSimpleName();
        }

        @Override
        public ServletContext getServletContext() {
            return MetricsController.this.getServletContext();
        }

        @Override
        public String getInitParameter(final String paramName) {
            return MetricsController.this.initParameters.getProperty(paramName);
        }

        @Override
        public Enumeration getInitParameterNames() {
            return MetricsController.this.initParameters.keys();
        }
    }
}
