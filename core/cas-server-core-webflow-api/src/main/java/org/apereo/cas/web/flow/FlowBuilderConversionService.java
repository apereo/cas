package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;

import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.convert.ConversionExecutorNotFoundException;
import org.springframework.binding.convert.service.DefaultConversionService;
import org.springframework.binding.convert.service.StaticConversionExecutor;

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
        addConverter(StringToCharArrayConverter.ID, StringToCharArrayConverter.INSTANCE);
    }

    @Override
    public ConversionExecutor getConversionExecutor(final String id, final Class<?> sourceClass, final Class<?> targetClass) throws ConversionExecutorNotFoundException {
        if (id.equals(StringToCharArrayConverter.ID)) {
            return new StaticConversionExecutor(sourceClass, targetClass, StringToCharArrayConverter.INSTANCE);
        }
        return super.getConversionExecutor(id, sourceClass, targetClass);
    }
}
