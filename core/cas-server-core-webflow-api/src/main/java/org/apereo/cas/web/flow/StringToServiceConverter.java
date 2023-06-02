package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;

import lombok.RequiredArgsConstructor;
import org.springframework.binding.convert.converters.Converter;

/**
 * This is {@link StringToServiceConverter}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
public class StringToServiceConverter implements Converter {
    private final ServiceFactory<WebApplicationService> factory;

    @Override
    public Class<?> getSourceClass() {
        return String.class;
    }

    @Override
    public Class<?> getTargetClass() {
        return Service.class;
    }

    @Override
    public Object convertSourceToTargetClass(final Object o, final Class<?> aClass) {
        return factory.createService(o.toString());
    }
}
