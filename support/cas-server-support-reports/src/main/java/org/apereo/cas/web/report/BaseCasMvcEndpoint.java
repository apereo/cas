package org.apereo.cas.web.report;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseCasMvcEndpoint.class);


    /**
     * Instantiates a new Base cas mvc endpoint.
     * Endpoints are by default sensitive.
     *
     * @param name     the name
     * @param path     the path
     * @param endpoint the endpoint
     */
    public BaseCasMvcEndpoint(final String name, final String path, final MonitorProperties.Endpoints.BaseEndpoint endpoint) {
        super(name, path, true);
        setSensitive(endpoint.isSensitive());
        setEnabled(endpoint.isEnabled());
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
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            throw new UnuauthorizedEndpointException();
        }
    }

    @ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Access Denied")
    private static class UnuauthorizedEndpointException extends RuntimeException {
        private static final long serialVersionUID = 3192230382776656678L;
    }
}
