package org.apereo.cas.services.util;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;
import org.apereo.cas.util.serialization.JacksonInjectableValueSupplier;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link BaseRegisteredServiceSerializer}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public abstract class BaseRegisteredServiceSerializer extends AbstractJacksonBackedStringSerializer<RegisteredService> {
    private static final long serialVersionUID = -86170670153712101L;

    /**
     * Application context used for decorating the object mapper, etc.
     */
    protected final ConfigurableApplicationContext applicationContext;

    protected BaseRegisteredServiceSerializer(final ConfigurableApplicationContext applicationContext) {
        super(new DefaultPrettyPrinter());
        this.applicationContext = applicationContext;
    }

    @Override
    protected void configureObjectMapper(final ObjectMapper mapper) {
        super.configureObjectMapper(mapper);
        val serializers = new ArrayList<>(applicationContext.getBeansOfType(RegisteredServiceSerializationCustomizer.class).values());
        AnnotationAwareOrderComparator.sort(serializers);
        val injectedValues = (Map) serializers
            .stream()
            .map(RegisteredServiceSerializationCustomizer::getInjectableValues)
            .flatMap(entry -> entry.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        mapper.setInjectableValues(new JacksonInjectableValueSupplier(() -> injectedValues));
        serializers.forEach(ser -> ser.customize(mapper));
    }
}
