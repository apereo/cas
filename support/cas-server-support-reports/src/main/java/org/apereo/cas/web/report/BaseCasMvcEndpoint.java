package org.apereo.cas.web.report;

import org.apache.commons.lang3.BooleanUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.monitor.MonitorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.mvc.AbstractNamedMvcEndpoint;
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
    /**
     * The constant LOGGER.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseCasMvcEndpoint.class);


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
        super(name, path, true);

        final boolean s = casProperties.getMonitor().getEndpoints().isSensitive() || BooleanUtils.toBoolean(endpoint.isSensitive());
        setSensitive(s);
        
        final boolean b = casProperties.getMonitor().getEndpoints().isEnabled() || BooleanUtils.toBoolean(endpoint.isEnabled());
        setEnabled(b);
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
