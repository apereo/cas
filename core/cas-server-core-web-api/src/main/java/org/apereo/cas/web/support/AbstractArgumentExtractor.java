package org.apereo.cas.web.support;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for handling argument extraction.
 *
 * @author Scott Battaglia
 * @since 3.1.2
 */
public abstract class AbstractArgumentExtractor implements ArgumentExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractArgumentExtractor.class);
    
    /**
     * The factory responsible for creating service objects based on the arguments extracted.
     */
    protected List<ServiceFactory<? extends WebApplicationService>> serviceFactoryList;

    /**
     * Default extractor initiation.
     */
    public AbstractArgumentExtractor() {
        this.serviceFactoryList = new ArrayList<>();
    }

    /**
     * Instantiates a new argument extractor.
     *
     * @param serviceFactory the service factory
     */
    public AbstractArgumentExtractor(final ServiceFactory<? extends WebApplicationService> serviceFactory) {
        this.serviceFactoryList = new ArrayList<>();
        this.serviceFactoryList.add(serviceFactory);
    }

    /**
     * Instantiates a new argument extractor.
     *
     * @param serviceFactoryList the service factory list
     */
    public AbstractArgumentExtractor(final List<ServiceFactory<? extends WebApplicationService>> serviceFactoryList) {
        this.serviceFactoryList = new ArrayList<>();
        this.serviceFactoryList.addAll(serviceFactoryList);
    }

    @Override
    public WebApplicationService extractService(final HttpServletRequest request) {
        final WebApplicationService service = extractServiceInternal(request);

        if (service == null) {
            LOGGER.debug("Extractor did not generate service.");
        } else {
            LOGGER.debug("Extractor generated service type [{}] for: [{}]", service.getClass().getName(), service.getId());
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
        return this.serviceFactoryList.get(0);
    }

    @Override
    public List<ServiceFactory<? extends WebApplicationService>> getServiceFactories() {
        return this.serviceFactoryList;
    }

}
