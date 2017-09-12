package org.apereo.cas.support.events.dao;

import org.apereo.cas.influxdb.InfluxDbConnectionFactory;

import javax.annotation.PreDestroy;
import java.util.Collection;

/**
 * This is {@link InfluxDbCasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class InfluxDbCasEventRepository extends AbstractCasEventRepository {
    private final InfluxDbConnectionFactory influxDbConnectionFactory;

    public InfluxDbCasEventRepository(final InfluxDbConnectionFactory influxDbConnectionFactory) {
        this.influxDbConnectionFactory = influxDbConnectionFactory;
    }

    @Override
    public void save(final CasEvent event) {

    }

    @Override
    public Collection<CasEvent> load() {
        return null;
    }

    @Override
    public Collection<CasEvent> getEventsForPrincipal(final String id) {
        return null;
    }

    /**
     * Stops the database client.
     */
    @PreDestroy
    public void destroy() {
        try {
            LOGGER.debug("Shutting down Couchbase");
            this.influxDbConnectionFactory.close();
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
