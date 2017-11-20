package org.apereo.cas.web;

import org.apache.commons.lang3.BooleanUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.monitor.MonitorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.mvc.AbstractNamedMvcEndpoint;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link BaseCasMvcEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseCasMvcEndpoint extends AbstractNamedMvcEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseCasMvcEndpoint.class);
    private static final Boolean DEFAULT_SENSITIVE_VALUE = Boolean.TRUE;

    /**
     * CAS settings.
     */
    protected final CasConfigurationProperties casProperties;

    /**
     * App context.
     */
    @Autowired
    protected ApplicationContext applicationContext;

    /**
     * Instantiates a new Base cas mvc endpoint.
     * Endpoints are by default sensitive.
     *
     * @param name          the name
     * @param path          the path
     * @param endpoint      the endpoint
     * @param casProperties the cas properties
     */
    public BaseCasMvcEndpoint(final String name, final String path,
                              final MonitorProperties.BaseEndpoint endpoint,
                              final CasConfigurationProperties casProperties) {
        super(name, path, DEFAULT_SENSITIVE_VALUE);
        this.casProperties = casProperties;

        setEndpointSensitivity(endpoint, casProperties);
        setEndpointCapability(endpoint, casProperties);

    }

    private void setEndpointSensitivity(final MonitorProperties.BaseEndpoint endpoint,
                                        final CasConfigurationProperties casProperties) {
        final String endpointName = endpoint.getClass().getSimpleName();
        if (endpoint.isSensitive() == null) {
            LOGGER.trace("Sensitivity for endpoint [{}] is undefined. Checking defaults...", endpointName);
            final Boolean defaultSensitive = casProperties.getMonitor().getEndpoints().isSensitive();
            if (defaultSensitive != null) {
                final boolean s = BooleanUtils.toBoolean(defaultSensitive);
                setSensitive(s);
                LOGGER.trace("Default sensitivity for endpoint [{}] is set to [{}]", endpointName, s);
            } else {
                LOGGER.trace("Default sensitivity for endpoint [{}] is undefined.", endpointName);
                setSensitive(DEFAULT_SENSITIVE_VALUE);
            }
        } else {
            final boolean s = BooleanUtils.toBoolean(endpoint.isSensitive());
            setSensitive(s);
            LOGGER.trace("Explicitly marking endpoint [{}] sensitivity as [{}]", endpointName, s);
        }
    }

    /**
     * Is endpoint capable ?
     *
     * @param endpoint      the endpoint
     * @param casProperties the cas properties
     * @return the boolean
     */
    protected static boolean isEndpointCapable(final MonitorProperties.BaseEndpoint endpoint,
                                               final CasConfigurationProperties casProperties) {
        final String endpointName = endpoint.getClass().getSimpleName();
        if (endpoint.isEnabled() == null) {
            LOGGER.trace("Capability for endpoint [{}] is undefined. Checking defaults...", endpointName);
            final Boolean defaultEnabled = casProperties.getMonitor().getEndpoints().isEnabled();
            if (defaultEnabled != null) {
                final boolean s = BooleanUtils.toBoolean(defaultEnabled);
                LOGGER.trace("Default capability for endpoint [{}] is set to [{}]", endpointName, s);
                return s;
            }
            LOGGER.trace("Default capability for endpoint [{}] is undefined.", endpointName);
            return false;

        }
        final boolean s = BooleanUtils.toBoolean(endpoint.isEnabled());
        LOGGER.trace("Explicitly marking endpoint [{}] capability as [{}]", endpointName, s);
        return s;
    }

    private void setEndpointCapability(final MonitorProperties.BaseEndpoint endpoint,
                                       final CasConfigurationProperties casProperties) {
        final String endpointName = endpoint.getClass().getSimpleName();
        final boolean s = isEndpointCapable(endpoint, casProperties);
        LOGGER.trace("Finalized capability for endpoint [{}] is [{}].", endpointName, s);
        setEnabled(s);
    }

    /**
     * Ensure endpoint access is authorized.
     *
     * @param request  the request
     * @param response the response
     */
    protected void ensureEndpointAccessIsAuthorized(final HttpServletRequest request,
                                                    final HttpServletResponse response) {
        if (!isEnabled()) {
            LOGGER.warn("Access to endpoint [{}] is not enabled", getName());
            throw new UnuauthorizedEndpointException();
        }
    }

    /**
     * The type Unuauthorized endpoint exception.
     */
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Access Denied")
    private static class UnuauthorizedEndpointException extends RuntimeException {
        /**
         * The constant serialVersionUID.
         */
        private static final long serialVersionUID = 3192230382776656678L;
    }
}
