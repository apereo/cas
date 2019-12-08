package org.apereo.cas.web.support;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.util.DigestUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for handling argument extraction.
 *
 * @author Scott Battaglia
 * @since 3.1.2
 */
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Getter
public abstract class AbstractArgumentExtractor implements ArgumentExtractor {

    /**
     * The factory responsible for creating service objects based on the arguments extracted.
     */
    protected List<ServiceFactory<? extends WebApplicationService>> serviceFactories = new ArrayList<>(0);

    @Override
    public WebApplicationService extractService(final HttpServletRequest request) {
        val service = extractServiceInternal(request);
        if (service == null) {
            LOGGER.trace("Extractor did not generate service via [{}].", getClass().getName());
        } else {
            LOGGER.trace("Extractor [{}] generated service type [{}] for: [{}]",
                getClass().getName(),
                service.getClass().getName(), DigestUtils.abbreviate(service.getId()));
        }
        return service;
    }

    /**
     * Extract service from the request.
     *
     * @param request the request
     * @return the web application service
     */
    protected abstract WebApplicationService extractServiceInternal(HttpServletRequest request);

    public ServiceFactory<? extends WebApplicationService> getServiceFactory() {
        return this.serviceFactories.get(0);
    }

}
