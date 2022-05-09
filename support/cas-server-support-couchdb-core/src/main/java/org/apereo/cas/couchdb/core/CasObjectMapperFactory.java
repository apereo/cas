package org.apereo.cas.couchdb.core;

import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.ektorp.impl.StdObjectMapperFactory;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link CouchDbConnectorFactory}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@RequiredArgsConstructor
public class CasObjectMapperFactory extends StdObjectMapperFactory {
    private final ConfigurableApplicationContext applicationContext;

    @Override
    protected void applyDefaultConfiguration(final ObjectMapper om) {
        super.applyDefaultConfiguration(om);
        JacksonObjectMapperFactory.configure(applicationContext, om);
    }
}
