package org.apereo.cas.services.util;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.Serial;

/**
 * This is {@link BaseRegisteredServiceSerializer}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public abstract class BaseRegisteredServiceSerializer extends AbstractJacksonBackedStringSerializer<RegisteredService> {
    @Serial
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
        JacksonObjectMapperFactory.configure(applicationContext, mapper);
    }
}
