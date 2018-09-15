package org.apereo.cas.couchdb.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.ektorp.impl.StdObjectMapperFactory;

/**
 * This is {@link CouchDbConnectorFactory}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public class CasObjectMapperFactory extends StdObjectMapperFactory {

    @Override
    protected void applyDefaultConfiguration(final ObjectMapper om) {
        super.applyDefaultConfiguration(om);
        om.registerModule(new JavaTimeModule());
    }
}
