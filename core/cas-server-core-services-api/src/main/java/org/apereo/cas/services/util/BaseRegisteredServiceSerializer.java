package org.apereo.cas.services.util;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link BaseRegisteredServiceSerializer}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public abstract class BaseRegisteredServiceSerializer extends AbstractJacksonBackedStringSerializer<RegisteredService> {
    private static final long serialVersionUID = -86170670153712101L;

    protected final ConfigurableApplicationContext applicationContext;

    protected BaseRegisteredServiceSerializer(final ConfigurableApplicationContext applicationContext) {
        super(new DefaultPrettyPrinter());
        this.applicationContext = applicationContext;
    }
}
