package org.apereo.cas.web.report;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CasVersion;
import org.apereo.cas.util.InetAddressUtils;
import org.apereo.cas.web.BaseCasMvcEndpoint;
import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;


/**
 * Reports overall CAS health based on the observations of the configured {@link HealthEndpoint} instance.
 *
 * @author Marvin S. Addison
 * @since 3.5
 */
@Slf4j
public class StatusController extends BaseCasMvcEndpoint {
    private final HealthEndpoint healthEndpoint;

    public StatusController(final CasConfigurationProperties casProperties, final HealthEndpoint healthEndpoint) {
        super("status", StringUtils.EMPTY, casProperties.getMonitor().getEndpoints().getStatus(), casProperties);
        this.healthEndpoint = healthEndpoint;
    }

    /**
     * Handle request.
     *
     * @param request  the request
     * @param response the response
     * @throws Exception the exception
     */
    @GetMapping
    @ResponseBody
    protected void handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        ensureEndpointAccessIsAuthorized(request, response);
        final StringBuilder sb = new StringBuilder();
        final Health health = this.healthEndpoint.invoke();
        final Status status = health.getStatus();
        
        if (status.equals(Status.DOWN) || status.equals(Status.OUT_OF_SERVICE)) {
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        }

        sb.append("Health: ").append(status.getCode());
        sb.append("\n\nHost:\t\t").append(
            StringUtils.isBlank(casProperties.getHost().getName())
                ? InetAddressUtils.getCasServerHostName()
                : casProperties.getHost().getName()
        );
        sb.append("\nServer:\t\t").append(casProperties.getServer().getName());
        sb.append("\nVersion:\t").append(CasVersion.getVersion());
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        try (Writer writer = response.getWriter()) {
            IOUtils.copy(new ByteArrayInputStream(sb.toString().getBytes(response.getCharacterEncoding())),
                writer,
                StandardCharsets.UTF_8);
            writer.flush();
        }
    }
}
