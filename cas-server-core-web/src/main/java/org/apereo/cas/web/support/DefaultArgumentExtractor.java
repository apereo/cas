package org.apereo.cas.web.support;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * The default argument extractor is responsible for creating service
 * objects based on requests. The task of creating services is delegated to
 * a service factory that is pluggable for each instance of the extractor.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@RefreshScope
@Component("defaultArgumentExtractor")
public class DefaultArgumentExtractor extends AbstractArgumentExtractor {

    /**
     * Default extractor.
     */
    public DefaultArgumentExtractor() {
    }
    
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
        return getServiceFactoryList().stream().map(factory -> {
            final WebApplicationService service = factory.createService(request);
            if (service != null) {
                logger.debug("Created {} based on {}", service, factory);
                return service;
            }
            return null;
        }).filter(service -> service != null).findFirst().orElseGet(() -> {
            logger.debug("No service could be extracted based on the given request");
            return null;
        });
    }

    @Resource(name="serviceFactoryList")
    public void setServiceFactoryList(final List<ServiceFactory<? extends WebApplicationService>> serviceFactoryList) {
        this.serviceFactoryList = serviceFactoryList;
    }

}
