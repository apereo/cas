package org.jasig.cas.web.support;

import org.jasig.cas.authentication.principal.ServiceFactory;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for handling argument extraction.
 *
 * @author Scott Battaglia
 * @since 3.1.2
 *
 */
public abstract class AbstractArgumentExtractor implements ArgumentExtractor {

    /** Logger instance. */
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    /** The factory responsible for creating service objects based on the arguments extracted. */
    @Resource(name="serviceFactoryList")
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
    public final WebApplicationService extractService(final HttpServletRequest request) {
        final WebApplicationService service = extractServiceInternal(request);

        if (service == null) {
            logger.debug("Extractor did not generate service.");
        } else {
            logger.debug("Extractor generated service for: {}", service.getId());
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

    public final ServiceFactory<? extends WebApplicationService> getServiceFactory() {
        return serviceFactoryList.get(0);
    }

    protected final List<ServiceFactory<? extends WebApplicationService>> getServiceFactories() {
        return serviceFactoryList;
    }

}
