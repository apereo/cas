package org.jasig.cas.web.support;


import org.apache.commons.lang3.NotImplementedException;
import org.jasig.cas.authentication.principal.ServiceFactory;
import org.jasig.cas.authentication.principal.WebApplicationService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @deprecated As of 4.2, use {@link DefaultArgumentExtractor}.
 * Implements the traditional CAS2 protocol.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Deprecated
public final class CasArgumentExtractor extends AbstractArgumentExtractor {
    /**
     * Create a CAS argument extractor.
     * @param serviceFactory service factory
     */
    public CasArgumentExtractor(final ServiceFactory<? extends WebApplicationService> serviceFactory) {
        super(serviceFactory);
    }

    /**
     *  create cas argument extractor.
     * @param serviceFactoryList list of service factories
     */
    public CasArgumentExtractor(final List<ServiceFactory<? extends WebApplicationService>> serviceFactoryList) {
        super(serviceFactoryList);
    }

    @Override
    public WebApplicationService extractServiceInternal(final HttpServletRequest request) {
        throw new NotImplementedException("This operation is not supported. "
                + "The class is deprecated and will be removed in future versions");
    }
}


