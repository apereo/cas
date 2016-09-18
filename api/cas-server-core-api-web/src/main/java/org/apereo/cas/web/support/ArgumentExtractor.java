package org.apereo.cas.web.support;

import javax.servlet.http.HttpServletRequest;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;

import java.util.List;

/**
 * Strategy interface for retrieving services from the request.
 *
 * @author Scott Battatglia
 * @since 3.1
 */
public interface ArgumentExtractor {
    /**
     * Retrieve the service from the request.
     *
     * @param request the request context.
     * @return the fully formed Service or null if it could not be found.
     */
    WebApplicationService extractService(HttpServletRequest request);

    /**
     * Gets service factories.
     *
     * @return the service factories
     */
    List<ServiceFactory<? extends WebApplicationService>> getServiceFactories();
}
