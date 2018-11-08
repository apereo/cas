package org.apereo.cas.web.support;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.util.CollectionUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

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
@Slf4j
@Getter
public class DefaultArgumentExtractor extends AbstractArgumentExtractor {

    /**
     * Instantiates a new argument extractor.
     *
     * @param serviceFactory the service factory
     */
    public DefaultArgumentExtractor(final ServiceFactory<? extends WebApplicationService> serviceFactory) {
        super(CollectionUtils.wrapList(serviceFactory));
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
            val service = factory.createService(request);
            if (service != null) {
                LOGGER.trace("Created [{}] based on [{}]", service, factory);
                return service;
            }
            return null;
        }).filter(Objects::nonNull).findFirst().orElseGet(() -> {
            LOGGER.trace("No service could be extracted based on the given request");
            return null;
        });
    }
}
