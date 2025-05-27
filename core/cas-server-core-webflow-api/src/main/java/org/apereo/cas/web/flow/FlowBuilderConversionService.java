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

    /**
     * This is necessary here, because the default executor
     * will wrap our converter inside a {@link org.springframework.binding.convert.converters.ObjectToArray}
     * component that interferes with the conversion. This conversion is mainly applicable when
     * passwords in form of {@link String} need to bind over to character arrays.
     *
     * @param id          identifier of the converter.
     * @param sourceClass source class of the binding object
     * @param targetClass target class of the binding object
     * @return conversion executor
     */
    @Override
    public ConversionExecutor getConversionExecutor(final String id, final Class<?> sourceClass, final Class<?> targetClass) throws ConversionExecutorNotFoundException {
        if (id.equals(StringToCharArrayConverter.ID)) {
            return new StaticConversionExecutor(sourceClass, targetClass, StringToCharArrayConverter.INSTANCE);
        }
        return super.getConversionExecutor(id, sourceClass, targetClass);
    }
}
