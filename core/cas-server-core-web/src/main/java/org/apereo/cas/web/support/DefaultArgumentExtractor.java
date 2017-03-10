package org.apereo.cas.web.support;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

/**
 * The default argument extractor is responsible for creating service
 * objects based on requests. The task of creating services is delegated to
 * a service factory that is pluggable for each instance of the extractor.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class DefaultArgumentExtractor extends AbstractArgumentExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultArgumentExtractor.class);
    
    /**
     * Instantiates a new argument extractor.
     *
     * @param serviceFactory the service factory
     */
    public DefaultArgumentExtractor(final ServiceFactory<? extends WebApplicationService> serviceFactory) {
        super(serviceFactory);
    }

    /**
     * Instantiates a new argument extractor.
     *
     * @param serviceFactoryList the service factory list
     */
    public DefaultArgumentExtractor(final List<ServiceFactory<? extends WebApplicationService>> serviceFactoryList) {
        super(serviceFactoryList);
    }

    @Override
    public WebApplicationService extractServiceInternal(final HttpServletRequest request) {
        return getServiceFactories().stream().map(factory -> {
            final WebApplicationService service = factory.createService(request);
            if (service != null) {
                LOGGER.debug("Created [{}] based on [{}]", service, factory);
                return service;
            }
            return null;
        }).filter(Objects::nonNull).findFirst().orElseGet(() -> {
            LOGGER.debug("No service could be extracted based on the given request");
            return null;
        });
    }
}
