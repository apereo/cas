package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;

import org.springframework.binding.convert.service.DefaultConversionService;

/**
 * Special conversion service with a {@link CompositeFlowExecutionKeyConverter}.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
public class FlowBuilderConversionService extends DefaultConversionService {

    public FlowBuilderConversionService(
        final ServiceFactory<WebApplicationService> webApplicationServiceFactory) {
        addConverter(new CompositeFlowExecutionKeyConverter());
        addConverter(new StringToServiceConverter(webApplicationServiceFactory));
    }
}
