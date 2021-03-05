package org.apereo.cas.web.report;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CasVersion;
import org.apereo.cas.util.InetAddressUtils;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
 * @deprecated since 6.2.0
 */
@Slf4j
@Endpoint(id = "status", enableByDefault = false)
@Deprecated(since ="6.2.0")
public class StatusEndpoint extends BaseCasActuatorEndpoint {
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
        val model = new LinkedHashMap<String, Object>();
        if (healthEndpoint == null) {
            model.put("status", HttpStatus.OK.value());
            model.put("description", HttpStatus.OK.name());
            LOGGER.info("Health endpoint is undefined/disabled. No health indicators may be consulted to query for health data "
                + "and the status results are always going to be [{}]", model);
        } else {
            val health = this.healthEndpoint.health();
            val status = health.getStatus();

            if (status.equals(Status.DOWN) || status.equals(Status.OUT_OF_SERVICE)) {
                model.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
                model.put("description", HttpStatus.SERVICE_UNAVAILABLE.name());
            } else {
                model.put("status", HttpStatus.OK.value());
                model.put("description", HttpStatus.OK.name());
            }
            model.put("health", status.getCode());
        }
        val hostname = casProperties.getHost().getName();
        model.put("host", StringUtils.isBlank(hostname)
            ? InetAddressUtils.getCasServerHostName()
            : hostname);
        model.put("server", casProperties.getServer().getName());
        model.put("version", CasVersion.asString());
        return model;
    }
}
