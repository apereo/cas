package org.apereo.cas.web.report;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CasVersion;
import org.apereo.cas.util.InetAddressUtils;
import org.apereo.cas.web.BaseCasMvcEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Reports overall CAS health based on the observations of the configured {@link HealthEndpoint} instance.
 *
 * @author Marvin S. Addison
 * @since 3.5
 */
@Slf4j
@Endpoint(id = "status")
public class StatusEndpoint extends BaseCasMvcEndpoint {
    private final HealthEndpoint healthEndpoint;

    public StatusEndpoint(final CasConfigurationProperties casProperties, final HealthEndpoint healthEndpoint) {
        super(casProperties);
        this.healthEndpoint = healthEndpoint;
    }

    /**
     * Handle request.
     *
     * @return the map
     */
    @ReadOperation
    public Map<String, Object> handle() {

        final var model = new LinkedHashMap<String, Object>();
        final var health = this.healthEndpoint.health();
        final var status = health.getStatus();

        if (status.equals(Status.DOWN) || status.equals(Status.OUT_OF_SERVICE)) {
            model.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
            model.put("description", HttpStatus.SERVICE_UNAVAILABLE.name());
        } else {
            model.put("status", HttpStatus.OK.value());
            model.put("description", HttpStatus.OK.name());
        }
        model.put("health", status.getCode());
        model.put("host", StringUtils.isBlank(getCasProperties().getHost().getName())
            ? InetAddressUtils.getCasServerHostName()
            : getCasProperties().getHost().getName());
        model.put("server", getCasProperties().getServer().getName());
        model.put("version", CasVersion.asString());
        return model;
    }
}
