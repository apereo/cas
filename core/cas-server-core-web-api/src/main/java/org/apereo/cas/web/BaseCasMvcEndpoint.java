package org.apereo.cas.web;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.monitor.MonitorProperties;

/**
 * This is {@link BaseCasMvcEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
public abstract class BaseCasMvcEndpoint {
    /**
     * The Cas properties.
     */
    private final CasConfigurationProperties casProperties;

    /**
     * Instantiates a new Base cas mvc endpoint.
     * Endpoints are by default sensitive.
     *
     * @param endpoint      the endpoint
     * @param casProperties the cas properties
     */
    public BaseCasMvcEndpoint(final MonitorProperties.BaseEndpoint endpoint, final CasConfigurationProperties casProperties) {
        this.casProperties = casProperties;
    }


    /**
     * Is endpoint capable object.
     *
     * @param endpoint      the endpoint
     * @param casProperties the cas properties
     * @return the object
     */
    protected Object isEndpointCapable(final MonitorProperties.BaseEndpoint endpoint, final CasConfigurationProperties casProperties) {
        return false;
    }

    /**
     * Is spring boot endpoint enabled boolean.
     *
     * @param endpoint the endpoint
     * @return the boolean
     */
    protected boolean isSpringBootEndpointEnabled(final Object endpoint) {
        return false;
    }
}
